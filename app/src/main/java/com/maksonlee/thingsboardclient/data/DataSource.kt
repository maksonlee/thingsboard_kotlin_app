package com.maksonlee.thingsboardclient.data

import android.content.Context
import com.google.gson.Gson
import com.maksonlee.thingsboardclient.data.model.Credential
import com.maksonlee.thingsboardclient.data.model.LoggedInUser
import com.maksonlee.thingsboardclient.data.model.Token
import com.maksonlee.thingsboardclient.data.model.thingsboard.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

interface ThingsBoardAuthService {
    @POST("auth/login")
    fun login(@Body credential: Credential): Call<Token>

    @POST("auth/token")
    fun refreshToken(
        @Header("X-Authorization") token: String,
        @Header("Content-Type") type: String,
        @Body refreshToken: RefreshToken
    ): Call<Token>
}

interface ThingsBoardApiService {
    @GET("users?pageSize=100&page=0")
    fun getUsers(@Header("X-Authorization") token: String): Call<GetUsersResp>

    @GET("customer/{id}/deviceInfos?pageSize=100&page=0")
    fun getCustomerDeviceInfos(
        @Header("X-Authorization") token: String,
        @Path("id") id: String
    ): Call<GetCustomerDeviceInfosResp>
}

private const val HOST = "localhost"

object ThingsBoardApi {
    private const val BASE_URL: String = "https://$HOST/api/"

    private lateinit var retrofit: Retrofit

    fun config(context: Context, datasource: DataSource) {
        val client =
            OkHttpClient().newBuilder().authenticator(TokenAuthenticator(context, datasource))
                .build()
        retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    val retrofitService: ThingsBoardApiService by lazy {
        retrofit.create(ThingsBoardApiService::class.java)
    }
}

object ThingsBoardAuthApi {
    private const val BASE_URL: String = "https://$HOST/api/"

    private val client =
        OkHttpClient().newBuilder()
            .build()
    private var retrofit = Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: ThingsBoardAuthService by lazy {
        retrofit.create(ThingsBoardAuthService::class.java)
    }
}

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class DataSource(val context: Context) {

    private var token: Token
    private var customerId: String
    private val sharedPref =
        context.getSharedPreferences(
            "com.maksonlee.thingsboardclient.data.preference",
            Context.MODE_PRIVATE
        )

    lateinit var webSocket: WebSocket

    init {
        token =
            Token(
                sharedPref.getString("jwt", "")!!,
                sharedPref.getString("token", "")!!,
                sharedPref.getString("refreshToken", "")!!
            )
        customerId = sharedPref.getString("customerId", "")!!
        ThingsBoardApi.config(context, this)
    }

    fun subscribe(deviceId: String): Flow<Result<List<Telemetry>>> = callbackFlow {
        val gson = Gson()
        val current = System.currentTimeMillis()
        val webSocketListener = object : WebSocketListener() {
            val timeWindow = 1.minutes.toLong(DurationUnit.MILLISECONDS)
            val subscribeCmd = """{
                "entityDataCmds": [
                  {
                    "cmdId": "10",
                    "query": {
                      "entityFilter": {"type": "entityList", "entityType": "DEVICE", "entityList": ["$deviceId"]},
                      "keyFilters": [],
                      "pageLink": {"pageSize": "10", "page": "0", "dynamic": "false"},
                      "latestValues": [{"type": "TIME_SERIES", "key": "temperature"}]
                    },
                    "tsCmd": {
                      "keys": ["temperature"],
                      "startTs": "${current - timeWindow}",
                      "timeWindow": "$timeWindow",
                      "interval": "1000",
                      "agg": "NONE"
                    }
                  }
                ]
              }""".trimIndent()

            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                Timber.i("onOpen()")
                webSocket.send(subscribeCmd)
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                Timber.i("onMessage()")
                Timber.i(text)
                val telemetrySubscribeResp = gson.fromJson(text, TelemetrySubscribeResp::class.java)
                if (telemetrySubscribeResp.update != null) {
                    trySend(Result.Success(telemetrySubscribeResp.update!![0].timeseries["temperature"]!!))
                } else {
                    trySend(Result.Success(telemetrySubscribeResp.data!!.data[0].timeseries["temperature"]!!))
                }
            }

            override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                Timber.i("onClosing()")
                Timber.i(reason)
            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                Timber.i("onClosed()")
            }

            override fun onFailure(
                webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?
            ) {
                Timber.w("onFailure()")
                if (response != null) {
                    trySend(Result.Error(IOException(response.code().toString())))
                }
                Timber.w(t.message)
            }
        }

        val client =
            OkHttpClient().newBuilder().authenticator(TokenAuthenticator(context, this@DataSource))
                .build()
        val request =
            Request.Builder()
                .url("wss://$HOST/api/ws/plugins/telemetry?token=" + token.jwt)
                .build()
        webSocket = client.newWebSocket(request, webSocketListener)
        awaitClose {}
    }

    fun unSubscribe(deviceId: String) {
        val unSubscribeCmd = """{
                "tsSubCmds": [
                    {
                        "cmdId": "10",
                        "unsubscribe" : "true"
                    }
                ]
            }""".trimIndent()
        webSocket.send(unSubscribeCmd)
        webSocket.cancel()
    }

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        val response =
            ThingsBoardAuthApi.retrofitService.login(Credential(username, password)).awaitResponse()

        if (response.code() == 200) {
            with(sharedPref.edit()) {
                Timber.i(response.body()!!.token)
                putString("jwt", response.body()!!.token)
                putString("token", "Bearer  " + response.body()!!.token)
                putString("refreshToken", response.body()!!.refreshToken)
                apply()
                token = Token(
                    response.body()!!.token,
                    "Bearer " + response.body()!!.token,
                    response.body()!!.refreshToken
                )
                Timber.i(token.toString())
            }
            Timber.i(token.token)
            val resp = ThingsBoardApi.retrofitService.getUsers(token.token).awaitResponse()
            if (resp.code() == 200 && resp.body()!!.data.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putString("customerId", resp.body()!!.data[0].customerId.id)
                    apply()
                    customerId = resp.body()!!.data[0].customerId.id
                }
            }
            Timber.i(resp.body().toString())

            val user = LoggedInUser(UUID.randomUUID().toString(), username)
            return Result.Success(user)
        } else {
            return Result.Error(IOException(response.errorBody().toString()))
        }
    }

    fun refreshToken(token: String, type: String, refreshToken: RefreshToken): Result<Token> {
        val response =
            ThingsBoardAuthApi.retrofitService.refreshToken(token, type, refreshToken).execute()

        var newToken: Token
        if (response.code() == 200) {
            with(sharedPref.edit()) {
                Timber.i(response.body()!!.token)
                putString("jwt", response.body()!!.token)
                putString("token", "Bearer  " + response.body()!!.token)
                putString("refreshToken", response.body()!!.refreshToken)
                apply()
                newToken = Token(
                    response.body()!!.token,
                    "Bearer " + response.body()!!.token,
                    response.body()!!.refreshToken
                )
            }
            return Result.Success(newToken)
        } else {
            return Result.Error(IOException(response.errorBody().toString()))
        }
    }

    suspend fun getCustomerDeviceInfos(): Result<List<DeviceInfo>> {
        val response =
            ThingsBoardApi.retrofitService.getCustomerDeviceInfos(token.token, customerId)
                .awaitResponse()
        return if (response.code() == 200) {
            Result.Success(response.body()!!.data)
        } else {
            Result.Error(IOException(response.code().toString()))
        }
    }
}