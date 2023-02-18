package com.maksonlee.thingsboardclient.ui.device

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maksonlee.thingsboardclient.data.DataSource
import com.maksonlee.thingsboardclient.data.Repository

class DeviceListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceListViewModel::class.java)) {
            return DeviceListViewModel(
                repository = Repository(
                    dataSource = DataSource(context)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}