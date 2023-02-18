package com.maksonlee.thingsboardclient.data

import com.maksonlee.thingsboardclient.data.Result
import com.maksonlee.thingsboardclient.data.model.LoggedInUser
import com.maksonlee.thingsboardclient.data.model.thingsboard.DeviceInfo
import com.maksonlee.thingsboardclient.data.model.thingsboard.Telemetry
import kotlinx.coroutines.flow.Flow

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class Repository(val dataSource: DataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun subscribe(deviceId: String): Flow<Result<List<Telemetry>>> {
        return dataSource.subscribe(deviceId)
    }

    fun unSubscribe(deviceId: String) {
        dataSource.unSubscribe(deviceId)
    }

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    suspend fun getCustomerDeviceInfos(): Result<List<DeviceInfo>> {
        return dataSource.getCustomerDeviceInfos()
    }
}