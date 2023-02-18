package com.maksonlee.thingsboardclient.ui.device

import com.maksonlee.thingsboardclient.data.model.thingsboard.DeviceInfo

data class DeviceListFetchResult(
    val success: List<DeviceInfo>? = null,
    val error: Int? = null
)