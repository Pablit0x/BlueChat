package com.ps.bluechat.data.chat

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ps.bluechat.domain.chat.ScanningState
import com.ps.bluechat.util.TAG

class BluetoothAdapterReceiver(
    private val isBluetoothEnabled: (Boolean) -> Unit,
    private val isDiscovering : (ScanningState) -> Unit,
    private val isDeviceDiscoverable : (Boolean) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scanMode = intent?.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)
        val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        val intentAction = intent?.action

        Log.d(TAG, "scanMode = $scanMode, state = $state, intentAction = $intentAction")

        when(intentAction){
            BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                when(scanMode){
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> isDeviceDiscoverable(true)
                    else -> isDeviceDiscoverable(false)
                }
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
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