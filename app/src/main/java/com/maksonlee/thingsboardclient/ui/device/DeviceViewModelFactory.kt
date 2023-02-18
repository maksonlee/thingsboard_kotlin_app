package com.maksonlee.thingsboardclient.ui.device

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maksonlee.thingsboardclient.data.DataSource
import com.maksonlee.thingsboardclient.data.Repository

class DeviceViewModelFactory(private val context: Context, private val deviceId: String) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            return DeviceViewModel(
                repository = Repository(
                    dataSource = DataSource(context)
                ),
                deviceId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}