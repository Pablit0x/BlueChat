package com.ps.bluechat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val className = BluetoothViewModel::class.simpleName
    private val _state = MutableStateFlow(BluetoothUiState())
    private var deviceConnectionJob: Job? = null

    init {

        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)

    bluetoothController.registerBluetoothAdapterReceiver()
    bluetoothController.registerBluetoothDeviceReceiver()

}

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
    bluetoothController.isBluetoothEnabled, bluetoothController.isDeviceDiscoverable, combineState
) { isBluetoothEnabled, isDeviceDiscoverable, state ->
    state.copy(
        isBluetoothEnabled = isBluetoothEnabled, isDeviceDiscoverable = isDeviceDiscoverable
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

fun startScan() {
    Log.d(className, "startScan()")
    bluetoothController.startScanning()
}

fun stopScan() {
    Log.d(className, "stopScan()")
    bluetoothController.stopScanning()
}

fun observeIncomingConnections() {
    Log.d(className, "observeIncomingConnections()")
    _state.update { it.copy(isConnecting = true) }
    deviceConnectionJob = bluetoothController.startBluetoothServer().observe()
}

fun connectToDevice(device: BluetoothDeviceDomain) {
    Log.d(className, "connectToDevice(): ${device.deviceName}")
    _state.update { it.copy(isConnecting = true) }
    deviceConnectionJob = bluetoothController.connectToDevice(device).observe()
}

fun disconnectDevice() {
    deviceConnectionJob?.cancel()
    bluetoothController.closeConnection()
    _state.update { it.copy(isConnecting = false, isConnected = false) }
}

fun changeDeviceName(deviceName: String) {
    Log.d(className, "changeDeviceName(): $deviceName")
    bluetoothController.changeDeviceName(deviceName = deviceName)
}

fun enableBluetooth() {
    bluetoothController.enableBluetooth()
}

fun disableBluetooth() {
    bluetoothController.disableBluetooth()
}


fun enableDiscoverability() {
    bluetoothController.enableDiscoverability()
}

fun disableDiscoverability() {
    bluetoothController.disableDiscoverability()
}

private fun Flow<ConnectionResult>.observe(): Job {
    return onEach { result ->
        when (result) {
            ConnectionResult.ConnectionEstablished -> {
                _state.update {
                    it.copy(
                        isConnected = true, isConnecting = false, errorMessage = null
                    )
                }
            }
            is ConnectionResult.Error -> {
                _state.update {
                    it.copy(
                        isConnected = false, isConnecting = false, errorMessage = result.message
                    )
                }
            }
        }
    }.catch {
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnected = false, isConnecting = false
            )
        }
    }.launchIn(viewModelScope)
}

override fun onCleared() {
    super.onCleared()
    bluetoothController.release()
}
}