package com.ps.bluechat.presentation

import com.ps.bluechat.domain.chat.BluetoothDeviceDomain

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices : List<BluetoothDeviceDomain> = emptyList()
)
