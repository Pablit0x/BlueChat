package com.ps.bluechat.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BluetoothDeviceReceiver(
    private val onDeviceFound : (BluetoothDevice) -> Unit,
    private val onStateChanged: (isConnected: Boolean, BluetoothDevice) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentAction = intent?.action

        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_NAME)
        }

        when(intentAction){
            BluetoothDevice.ACTION_ACL_CONNECTED -> onStateChanged(true, device ?: return)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> onStateChanged(false, device ?: return)
            BluetoothDevice.ACTION_FOUND -> device?.let(onDeviceFound)
        }
    }

}