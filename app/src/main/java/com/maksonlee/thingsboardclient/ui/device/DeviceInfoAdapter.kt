package com.maksonlee.thingsboardclient.ui.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.maksonlee.thingsboardclient.R
import com.maksonlee.thingsboardclient.data.model.thingsboard.DeviceInfo

class DeviceInfoAdapter : RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder>() {
    var data = listOf<DeviceInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.id.text = item.id.id
        holder.name.text = item.name

        with(holder.itemView) {
            setOnClickListener { itemView ->
                val bundle = bundleOf("deviceId" to item.id.id, "deviceName" to item.name)
                itemView.findNavController()
                    .navigate(R.id.action_device_list_dest_to_device_dest, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.device_info_view, parent, false) as ConstraintLayout
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.device_item_id)
        val name: TextView = itemView.findViewById(R.id.device_item_name)
    }
}