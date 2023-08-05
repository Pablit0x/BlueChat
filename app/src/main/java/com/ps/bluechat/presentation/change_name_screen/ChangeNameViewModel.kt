package com.ps.bluechat.presentation.change_name_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.util.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangeNameViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {
    fun changeDeviceName(deviceName: String) {
        Log.d(TAG, "changeDeviceName(): $deviceName")
        bluetoothController.changeDeviceName(deviceName = deviceName)
    }
}