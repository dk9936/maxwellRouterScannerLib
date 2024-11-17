package com.example.myblelibrary

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket

class MyNsdService(context: Context) {
    private var nsdManager: NsdManager = context.applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    companion object {
        private const val TAG = "MyNsdService"
        private const val DEFAULT_SERVICE_TYPE = "_http._tcp."
    }

    private val _serviceScanningResponse = MutableLiveData<Result<MutableList<Device>>>()
    val serviceScanningResponse: LiveData<Result<MutableList<Device>>>get() = _serviceScanningResponse

    private val allDevices : MutableList<Device> = mutableListOf()

    @Suppress("DEPRECATION")
    fun startServiceDiscovery(serviceType: String = DEFAULT_SERVICE_TYPE, stopScanTimeout: Long = 10000) {
        stopDiscovery()
        allDevices.clear()
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "startServiceDiscovery: started for service type $serviceType")
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String?) {
                    Log.d(TAG, "onDiscoveryStarted: Service discovery started for $serviceType")
                    _serviceScanningResponse.postValue(Result.Loading)
                }

                override fun onServiceFound(service: NsdServiceInfo?) {
                    service?.let {

                        if (service.serviceType.contains(serviceType)) {

                            nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                                override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                                    serviceInfo?.let {
                                        val ipAddress = it.host.hostAddress
                                        val name = it.serviceName
                                        val device = Device(ip = ipAddress!!, name = name, isLive = false)
                                        Log.d(TAG, "onServiceResolved: Resolved $name at $ipAddress")
                                       allDevices.add(device)
                                    }
                                }

                                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                                    Log.e(TAG, "onResolveFailed: $serviceInfo, error code: $errorCode")
                                }
                            })
                        }
                    }
                }

                override fun onDiscoveryStopped(serviceType: String?) {
                    Log.d(TAG, "onDiscoveryStopped: Discovery stopped for $serviceType")
                }

                override fun onServiceLost(service: NsdServiceInfo?) {
                    Log.e(TAG, "onServiceLost: Service lost ${service?.serviceName}")

                }

                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e(TAG, "onStartDiscoveryFailed: Error code $errorCode")
                    stopDiscovery()
                    _serviceScanningResponse.postValue(Result.Error("Failed to start discover service."))
                }

                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e(TAG, "onStopDiscoveryFailed: Error code $errorCode")
                    stopDiscovery()
                    _serviceScanningResponse.postValue(Result.Error("Failed to stop discover service."))
                }
            }

            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            delay(stopScanTimeout)
            stopDiscovery()
            _serviceScanningResponse.postValue(Result.Success(allDevices))
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
                Log.d(TAG, "stopDiscovery: Discovery stopped")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "stopDiscovery: Listener not registered", e)
            } finally {
                discoveryListener = null
            }
        }
    }

    fun pingPort(ip: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, 80), 2000)
                    onResult(true)
                    Log.d("PingPort", "IP: $ip Port: 80 is reachable.")
                }
            } catch (e: Exception) {
                onResult(false)
                Log.e("PingPortError", "Failed to connect to IP: $ip Port: 80", e)
            }
        }
    }
}
