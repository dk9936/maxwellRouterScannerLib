package com.example.mybleapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDevices(private val devices: MutableList<Device>,private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<AdapterDevices.DeviceViewHolder>() {

    companion object{
        private const val TAG = "AdapterDevices"
    }
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.tv_name)
        val deviceIp: TextView = itemView.findViewById(R.id.tv_ip)
        val deviceStatus: ImageView = itemView.findViewById(R.id.iv_status)
        val checkStatus: ImageButton = itemView.findViewById(R.id.ib_ping)
        val pingLoading: ProgressBar = itemView.findViewById(R.id.progressBar)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_device_layout, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        holder.deviceName.text = device.name
        holder.deviceIp.text = device.ip


        if (device.isPinging){
            holder.checkStatus.visibility = View.GONE
            holder.pingLoading.visibility = View.VISIBLE
        }else{
            holder.checkStatus.visibility = View.VISIBLE
            holder.pingLoading.visibility = View.GONE
        }

        if (device.isLive) {
            holder.deviceStatus.setImageResource(R.drawable.green_dot)
        } else {
            holder.deviceStatus.setImageResource(R.drawable.red_dot)
        }

        holder.checkStatus.setOnClickListener {
            itemClickListener.onPingClick(device)
        }
    }

    fun updateDeviceStatus(device: Device) {
        val existingDevice = devices.find { it.ip == device.ip }

        existingDevice?.let {
            it.isLive  = device.isLive
            it.isPinging = device.isPinging
            notifyItemChanged(devices.indexOf(it))
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}
