package com.ps.bluechat.presentation.model

import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.domain.chat.ScanningState

data class BluetoothState(
    val deviceName: String? = null,
    val isBluetoothEnabled: Boolean = false,
    val isDeviceDiscoverable: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.IDLE,
    val scanningState: ScanningState = ScanningState.IDLE,
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val errorMessage: String? = null
)
