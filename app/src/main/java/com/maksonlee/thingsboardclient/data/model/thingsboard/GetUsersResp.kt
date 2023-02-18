package com.maksonlee.thingsboardclient.data.model.thingsboard

data class GetUsersResp(
    var data: List<User>
)

data class User(
    var customerId: CustomerId
)

data class CustomerId(
    var entityType: String,
    var id: String
)