package com.maksonlee.thingsboardclient.data.model.thingsboard

data class GetCustomerDeviceInfosResp(
    var data: List<DeviceInfo>
)

data class DeviceInfo(
    var id: DeviceId,
    var name: String
)

data class DeviceId(
    var id: String
)