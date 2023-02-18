package com.maksonlee.thingsboardclient.data.model.thingsboard

data class TelemetrySubscribeResp(
    var data: Data?,
    var update: List<TimeSeries>?
)

data class Data(
    var data: List<TimeSeries>
)

data class TimeSeries(
    var timeseries: Map<String, List<Telemetry>>
)

data class Telemetry(
    var ts: String,
    var value: String
)