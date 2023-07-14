package com.ps.bluechat.data.chat

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ps.bluechat.domain.chat.ScanningState

class BluetoothAdapterReceiver(
    private val isBluetoothEnabled: (Boolean) -> Unit,
    private val isDiscovering : (ScanningState) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> isBluetoothEnabled(true)
                    BluetoothAdapter.STATE_OFF -> isBluetoothEnabled(false)
                }
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> isDiscovering(ScanningState.DISCOVERING)
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> isDiscovering(ScanningState.NOT_DISCOVERING)
        }
    }

}