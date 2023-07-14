package com.ps.bluechat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
): ViewModel() {

    init {
        bluetoothController.registerBluetoothAdapterReceiver()
        bluetoothController.registerBluetoothDeviceReceiver()
    }

    private val className = BluetoothViewModel::class.simpleName
    private val _state = MutableStateFlow(BluetoothUiState())

    private val combineState = combine(
        bluetoothController.deviceName,
        bluetoothController.scanningState,
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { deviceName, scanningState, scannedDevices, pairedDevices, state ->
        state.copy(
            deviceName = deviceName,
            scanningState = scanningState,
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices
        )
    }

    val state = combine(
        bluetoothController.isBluetoothEnabled,
        combineState
    ) { isBluetoothEnabled, state ->
        state.copy(
            isBluetoothEnabled = isBluetoothEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan(){
        Log.d(className, "startScan()")
        bluetoothController.startScanning()
    }

    fun stopScan(){
        Log.d(className, "stopScan()")
        bluetoothController.stopScanning()
    }

    fun changeDeviceName(newName: String){
        Log.d(className, "changeDeviceName(): $newName")
        bluetoothController.changeDeviceName(newName = newName)
    }

    fun enableBluetooth(){
        bluetoothController.enableBluetooth()
    }

    fun disableBluetooth(){
        bluetoothController.disableBluetooth()
    }
}