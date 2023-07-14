package com.ps.bluechat.domain.chat

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val deviceName : StateFlow<String?>
    val scanningState: StateFlow<ScanningState>
    val isBluetoothEnabled: StateFlow<Boolean>
    val scannedDevices : StateFlow<List<BluetoothDevice>>
    val pairedDevices : StateFlow<List<BluetoothDevice>>


    fun enableBluetooth()
    fun disableBluetooth()
    fun registerBluetoothAdapterReceiver()
    fun registerBluetoothDeviceReceiver()
    fun changeDeviceName(newName: String)
    fun startScanning()
    fun stopScanning()
    fun release()
}