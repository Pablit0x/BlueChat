package com.ps.bluechat.presentation

import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ScanningState

data class BluetoothUiState(
    val deviceName: String? = null,
    val isBluetoothEnabled: Boolean = false,
    val scanningState: ScanningState = ScanningState.IDLE,
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices : List<BluetoothDeviceDomain> = emptyList()
)
