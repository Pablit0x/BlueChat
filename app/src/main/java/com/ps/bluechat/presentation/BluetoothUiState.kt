package com.ps.bluechat.presentation

import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.domain.chat.ScanningState

data class BluetoothUiState(
    val deviceName: String? = null,
    val connectedDevice: BluetoothDeviceDomain? = null,
    val isBluetoothEnabled: Boolean = false,
    val isDeviceDiscoverable: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.IDLE,
    val scanningState: ScanningState = ScanningState.IDLE,
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices : List<BluetoothDeviceDomain> = emptyList(),
    val messages : List<BluetoothMessage> = emptyList(),
    val errorMessage: String? = null
)
