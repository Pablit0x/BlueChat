package com.ps.bluechat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val foundDeviceReceiver = FoundDeviceReceiver{ foundDevice ->
        _scannedDevices.update{ devices ->
            val newDevice = foundDevice.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())

    init {
        updatePairedDevices()
    }


    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    override fun startScanning() {
        if(!hasPermission(Constants.BLUETOOTH_SCAN_PERMISSION)){
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopScanning() {
        if(!hasPermission(Constants.BLUETOOTH_SCAN_PERMISSION)){
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Constants.BLUETOOTH_CONNECT_PERMISSION)) {
            return
        }
        bluetoothAdapter?.bondedDevices?.map {
            it.toBluetoothDeviceDomain()
        }?.also { pairedDevices -> _pairedDevices.update { pairedDevices } }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}