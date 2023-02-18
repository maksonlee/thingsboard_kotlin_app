package com.maksonlee.thingsboardclient.ui.devicelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksonlee.thingsboardclient.data.Repository
import com.maksonlee.thingsboardclient.data.Result
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceListViewModel(private val repository: Repository) : ViewModel() {

    private val _fetchResult = MutableLiveData<DeviceListFetchResult>()
    val fetchResult: LiveData<DeviceListFetchResult> = _fetchResult

    fun fetch() {
        viewModelScope.launch {
            try {
                val result = repository.getCustomerDeviceInfos()
                if (result is Result.Success) {
                    _fetchResult.value = DeviceListFetchResult(success = result.data)
                } else if (result is Result.Error) {
                    _fetchResult.value =
                        DeviceListFetchResult(error = result.exception.message!!.toInt())
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}