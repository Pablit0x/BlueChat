package com.ps.bluechat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.util.TAG

class BluetoothDeviceReceiver(
    private val onDeviceFound : (BluetoothDevice) -> Unit,
    private val onStateChanged: (connectionState: ConnectionState, BluetoothDevice) -> Unit,
    private val onBondStateChanged: () -> Unit
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
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

        Log.d(TAG, "intentAction = $intentAction, device = $device")

        when(intentAction){
            BluetoothDevice.ACTION_ACL_CONNECTED -> onStateChanged(
                ConnectionState.ACTIVE,
                device ?: return
            )

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> onStateChanged(
                ConnectionState.IDLE,
                device ?: return
            )
            BluetoothDevice.ACTION_FOUND -> device?.let(onDeviceFound)
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> onBondStateChanged()
        }
    }

}