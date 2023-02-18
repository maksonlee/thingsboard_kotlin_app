package com.maksonlee.thingsboardclient.ui.login

import com.maksonlee.thingsboardclient.ui.login.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)