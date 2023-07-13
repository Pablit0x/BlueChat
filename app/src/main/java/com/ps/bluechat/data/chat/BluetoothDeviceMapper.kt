package com.ps.bluechat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain


@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        deviceName = name,
        address = address
    )
}