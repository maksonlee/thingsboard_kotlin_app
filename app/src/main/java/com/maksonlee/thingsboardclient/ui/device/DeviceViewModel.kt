package com.maksonlee.thingsboardclient.ui.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksonlee.thingsboardclient.data.Repository
import com.maksonlee.thingsboardclient.data.Result
import com.maksonlee.thingsboardclient.data.model.thingsboard.Telemetry
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceViewModel(private val repository: Repository, private val deviceId: String) :
    ViewModel() {
    var telemetries = ArrayList<Telemetry>()

    private val _fetchResult = MutableLiveData<DeviceFetchResult>()
    val fetchResult: LiveData<DeviceFetchResult> = _fetchResult

    fun subscribe() {
        telemetries.clear()
        viewModelScope.launch {
            repository.subscribe(deviceId).collect {
                if (it is Result.Success) {
                    telemetries.addAll(it.data.reversed())
                    while (telemetries[0].ts.toLong() < System.currentTimeMillis() - 60 * 1000) {
                        telemetries.removeFirst()
                    }
                    Timber.i(telemetries.size.toString())
                    _fetchResult.value = DeviceFetchResult(success = telemetries)
                } else if (it is Result.Error) {
                    _fetchResult.value =
                        DeviceFetchResult(error = it.exception.message!!.toInt())
                }
            }
        }
    }

    fun unSubscribe() {
        repository.unSubscribe(deviceId)
    }
}