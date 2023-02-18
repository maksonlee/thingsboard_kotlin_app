package com.maksonlee.thingsboardclient.data.model

data class Token(
    var jwt: String,
    var token: String,
    var refreshToken: String
)