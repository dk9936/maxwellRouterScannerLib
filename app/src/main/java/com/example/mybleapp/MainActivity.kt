package com.example.mybleapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybleapp.databinding.ActivityMainBinding
import com.example.myblelibrary.MyNsdService
import com.example.myblelibrary.Result




class MainActivity : AppCompatActivity(), ItemClickListener{
    private lateinit var binding: ActivityMainBinding

    private lateinit var myNsdService: MyNsdService
    private lateinit var adapterDevices: AdapterDevices

    companion object {
        private const val TAG = "NsdScanningTAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myNsdService = MyNsdService(this)

        showErrorScreen("Tap on Button to get all devices")
        
        myNsdService.serviceScanningResponse.observe(this){
            when(it){
                is Result.Loading->{
                    loadingScreen()
                    Log.d(TAG, "onCreate: started scanning")
                }
                is Result.Success->{
                    val devices = it.data
                    for (device in devices){
                        Log.d(TAG, "onCreate: $device")
                    }
                    val devicesToShow = mutableListOf<Device>()
                    for (device in devices){
                        val eachDevice = Device(device.ip, device.name, device.isLive, false)
                        devicesToShow.add(eachDevice)
                    }
                    showDevices(devicesToShow)
                }
                is Result.Error->{
                    showErrorScreen(it.message)
                    Log.d(TAG, "onCreate: error is ${it.message}")
                }
            }
        }



        binding.btnScan.setOnClickListener {
            myNsdService.startServiceDiscovery()
        }
    }

    private fun showErrorScreen(errorMessage: String){
        binding.progressBar.visibility = View.GONE

        binding.rvDevices.visibility = View.GONE

        binding.tvNoDevice.apply {
            text = errorMessage
            visibility = View.VISIBLE
        }
    }

    private fun loadingScreen(){
        binding.progressBar.visibility = View.VISIBLE
        binding.rvDevices.visibility = View.GONE
        binding.tvNoDevice.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showDevices(devices: MutableList<Device>){
        if (devices.isNotEmpty()){
            adapterDevices = AdapterDevices(devices,this)

            binding.progressBar.visibility = View.GONE
            binding.tvNoDevice.visibility = View.GONE
            binding.rvDevices.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = adapterDevices
                visibility = View.VISIBLE
            }
        }else{
            binding.progressBar.visibility = View.GONE
            binding.tvNoDevice.apply {
                text = "No devices found."
                visibility = View.VISIBLE
            }
            binding.rvDevices.visibility = View.GONE
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        myNsdService.stopDiscovery()
    }

    override fun onPingClick(device: Device) {
        device.isPinging = true
        adapterDevices.updateDeviceStatus(device)
        myNsdService.pingPort(device.ip){isLive->
            device.isPinging = false
            if (isLive){
                device.isLive = true
                adapterDevices.updateDeviceStatus(device)
            }else{
                device.isLive = false
                adapterDevices.updateDeviceStatus(device)
            }
        }
    }


}



