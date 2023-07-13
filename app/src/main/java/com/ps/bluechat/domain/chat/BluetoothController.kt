package com.ps.bluechat.domain.chat

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices : StateFlow<List<BluetoothDevice>>
    val pairedDevices : StateFlow<List<BluetoothDevice>>

    fun startScanning()
    fun stopScanning()
    fun release()
}