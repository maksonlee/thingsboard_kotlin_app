package com.maksonlee.thingsboardclient.ui.devicelist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.maksonlee.thingsboardclient.databinding.FragmentDeviceListBinding
import timber.log.Timber

class DeviceListFragment : Fragment() {

    private lateinit var deviceListViewModel: DeviceListViewModel
    private lateinit var binding: FragmentDeviceListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = DeviceInfoAdapter()
        binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        binding.deviceInfoList.adapter = adapter

        deviceListViewModel = ViewModelProvider(
            this,
            DeviceListViewModelFactory(requireContext())
        )[DeviceListViewModel::class.java]

        deviceListViewModel.fetchResult.observe(viewLifecycleOwner, Observer {
            val deviceListFetchResult = it

            if (deviceListFetchResult.error != null && deviceListFetchResult.error == 401) {
                Timber.e(deviceListFetchResult.error.toString())
                val sharedPref = requireContext().getSharedPreferences(
                    "com.maksonlee.thingsboardclient.data.preference",
                    Context.MODE_PRIVATE
                )
                with(sharedPref.edit()) {
                    remove("jwt")
                    remove("token")
                    remove("refreshToken")
                    remove("customerId")
                    apply()
                }
                findNavController().navigate(DeviceListFragmentDirections.actionDeviceListDestToLoginDest())
            }
            if (deviceListFetchResult.success != null) {
                adapter.data = deviceListFetchResult.success
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        deviceListViewModel.fetch()
    }
}