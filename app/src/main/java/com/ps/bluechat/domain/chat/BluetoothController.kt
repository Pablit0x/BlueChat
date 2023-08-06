package com.ps.bluechat.domain.chat

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val deviceName : StateFlow<String?>
    val scanningState: StateFlow<ScanningState>
    val connectedDevice: StateFlow<BluetoothDeviceDomain?>
    val isBluetoothEnabled: StateFlow<Boolean>
    val isDeviceDiscoverable: StateFlow<Boolean>
    val connectionState: StateFlow<ConnectionState>
    val scannedDevices : StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val messages: StateFlow<List<BluetoothMessage>>
    val errors: SharedFlow<String?>


    suspend fun trySendImage(uri : Uri) : BluetoothMessage?
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
    fun createBond(device: BluetoothDeviceDomain)
    fun removeBond(device: BluetoothDeviceDomain)
}