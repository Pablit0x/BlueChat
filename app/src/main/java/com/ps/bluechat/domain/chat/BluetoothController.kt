package com.ps.bluechat.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val deviceName : StateFlow<String?>
    val scanningState: StateFlow<ScanningState>
    val isBluetoothEnabled: StateFlow<Boolean>
    val isDeviceDiscoverable: StateFlow<Boolean>
    val isConnected: StateFlow<Boolean>
    val scannedDevices : StateFlow<List<BluetoothDevice>>
    val pairedDevices : StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>



    suspend fun trySendMessage(message: String) : BluetoothMessage?
    fun startBluetoothServer() : Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDomain) : Flow<ConnectionResult>
    fun closeConnection()
    fun enableDiscoverability()
    fun disableDiscoverability()
    fun enableBluetooth()
    fun disableBluetooth()
    fun registerBluetoothAdapterReceiver()
    fun registerBluetoothDeviceReceiver()
    fun changeDeviceName(deviceName: String)
    fun startScanning()
    fun stopScanning()
    fun updateDeviceName()
    fun release()
}