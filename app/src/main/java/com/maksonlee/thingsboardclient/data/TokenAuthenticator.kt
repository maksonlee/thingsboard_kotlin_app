package com.maksonlee.thingsboardclient.data

import android.content.Context
import com.maksonlee.thingsboardclient.data.DataSource
import com.maksonlee.thingsboardclient.data.Result
import com.maksonlee.thingsboardclient.data.model.Token
import com.maksonlee.thingsboardclient.data.model.thingsboard.RefreshToken
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class TokenAuthenticator(context: Context, private val datasource: DataSource) : Authenticator {
    private val sharedPref =
        context.getSharedPreferences("com.maksonlee.thingsboardclient.data.preference", Context.MODE_PRIVATE)

    override fun authenticate(route: Route?, response: Response): Request? {
        val token = sharedPref.getString("token", "")!!
        val refreshToken = sharedPref.getString("refreshToken", "")!!

        val result: Result<Token> =
            datasource.refreshToken(token, "application/json", RefreshToken(refreshToken))
        var newToken = ""
        var newRequest = response.request()
        var url = response.request().url()
        if (result is Result.Success) {
            if (url.encodedPath().equals("/api/ws/plugins/telemetry")) {
                newToken = result.data.jwt
                url = url.newBuilder()
                    .setQueryParameter("token", newToken).build()
                newRequest = newRequest.newBuilder().url(url).build()
            } else {
                newToken = result.data.token
                newRequest = newRequest.newBuilder().removeHeader("X-Authorization")
                    .addHeader("X-Authorization", newToken).build()
            }
        }

        return newRequest
    }
}
