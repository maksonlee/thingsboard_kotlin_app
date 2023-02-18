package com.maksonlee.thingsboardclient.ui.device

import com.maksonlee.thingsboardclient.data.model.thingsboard.Telemetry

data class DeviceFetchResult(
    val success: List<Telemetry>? = null,
    val error: Int? = null
)