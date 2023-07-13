package com.ps.bluechat.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val deviceName: String?,
    val address: String
)
