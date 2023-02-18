package com.maksonlee.thingsboardclient.ui.device

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.maksonlee.thingsboardclient.databinding.FragmentDeviceBinding
import com.maksonlee.thingsboardclient.ui.device.DeviceViewModel
import com.maksonlee.thingsboardclient.ui.device.DeviceViewModelFactory
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class DeviceFragment : Fragment() {

    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var binding: FragmentDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceBinding.inflate(inflater, container, false)

        val temperature = binding.temperature

        val deviceId = requireArguments().getString("deviceId", "")
        val deviceName = requireArguments().getString("deviceName", "")

        deviceViewModel = ViewModelProvider(
            this, DeviceViewModelFactory(
                requireContext(),
                deviceId
            )
        )[DeviceViewModel::class.java]

        binding.deviceId.text = deviceName

        val chart = binding.chart
        val current = System.currentTimeMillis()
        configChart(chart, current)

        deviceViewModel.fetchResult.observe(viewLifecycleOwner, Observer {
            if (it.error != null && it.error == 401) {
                Timber.e(it.error.toString())
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
                findNavController().navigate(DeviceFragmentDirections.actionDeviceDestToLoginDest())
            }
            if (it.success != null) {
                temperature.text = it.success.last().value
                val entries = it.success.map {
                    Entry(
                        (it.ts.toLong() - current).toFloat(),
                        it.value.toFloat()
                    )
                } as ArrayList<Entry>
                plotChart(chart, entries)
            }
        })

        return binding.root
    }

    override fun onResume() {
        deviceViewModel.subscribe()
        super.onResume()
    }

    override fun onPause() {
        deviceViewModel.unSubscribe()
        super.onPause()
    }

    private fun configChart(chart: LineChart, base: Long) {
        chart.setBackgroundColor(Color.LTGRAY)
        val desc = Description()
        desc.text = ""
        chart.description = desc

        val sf = SimpleDateFormat("mm:ss")
        val xAxisFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return sf.format(Date(base + value.toLong()))
            }
        }

        val xAxis = chart.xAxis
        with(xAxis) {
            position = XAxisPosition.BOTTOM
            textSize = 10f
            setDrawAxisLine(true)
            setDrawGridLines(false)
            valueFormatter = xAxisFormatter
        }

        val yLeftAxis = chart.axisLeft
        with(yLeftAxis) {
            textSize = 10f
            axisMaximum = 100f
            axisMinimum = 0f
            setDrawAxisLine(true)
            setDrawGridLines(true)
            setDrawTopYLabelEntry(false)
        }

        val yRightAxis = chart.axisRight
        with(yRightAxis) {
            setDrawGridLines(false)
            setDrawTopYLabelEntry(false)
            setDrawLabels(false)
        }
    }

    private fun plotChart(chart: LineChart, entries: ArrayList<Entry>) {
        val dataSet = LineDataSet(entries, "Temperature")
        dataSet.setDrawCircles(false)
        dataSet.color = Color.BLUE
        val lineData = LineData(dataSet)
        lineData.setDrawValues(false)
        chart.data = lineData
        chart.invalidate()
    }
}