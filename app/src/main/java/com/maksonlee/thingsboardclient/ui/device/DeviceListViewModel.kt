package com.maksonlee.thingsboardclient.ui.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksonlee.thingsboardclient.data.Repository
import com.maksonlee.thingsboardclient.data.Result
import kotlinx.coroutines.launch

class DeviceListViewModel(private val repository: Repository) : ViewModel() {

    private val _fetchResult = MutableLiveData<DeviceListFetchResult>()
    val fetchResult: LiveData<DeviceListFetchResult> = _fetchResult

    fun fetch() {
        viewModelScope.launch {
            val result = repository.getCustomerDeviceInfos()
            if (result is Result.Success) {
                _fetchResult.value = DeviceListFetchResult(success = result.data)
            } else if (result is Result.Error) {
                _fetchResult.value =
                    DeviceListFetchResult(error = result.exception.message!!.toInt())
            }
        }
    }
}