package com.ps.bluechat.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionResult
import com.ps.bluechat.domain.chat.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val tag = BluetoothViewModel::class.simpleName

    private val _state = MutableStateFlow(BluetoothUiState())
    private var deviceConnectionJob: Job? = null

    init {

        bluetoothController.connectionState.onEach { connectionState ->
            _state.update { it.copy(connectionState = connectionState) }
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
        bluetoothController.isBluetoothEnabled,
        bluetoothController.isDeviceDiscoverable,
        bluetoothController.connectedDevice,
        combineState
    ) { isBluetoothEnabled, isDeviceDiscoverable, connectedDevice, state ->
        state.copy(
            isBluetoothEnabled = isBluetoothEnabled,
            isDeviceDiscoverable = isDeviceDiscoverable,
            connectedDevice = connectedDevice,
            messages = state.messages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        Log.d(tag, "startScan()")
        bluetoothController.startScanning()
    }

    fun createBond(device: BluetoothDeviceDomain) {
        Log.d(tag, "createBond(): $device")
        bluetoothController.createBond(device = device)
    }

    fun removeBond(device: BluetoothDeviceDomain) {
        Log.d(tag, "removeBond(): $device")
        bluetoothController.removeBond(device = device)
    }

    fun stopScan() {
        Log.d(tag, "stopScan()")
        bluetoothController.stopScanning()
    }

    fun observeIncomingConnections() {
        Log.d(tag, "observeIncomingConnections()")
        _state.update { it.copy(connectionState = ConnectionState.CONNECTION_OPEN) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().observe()
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        Log.d(tag, "connectToDevice(): ${device.deviceName}")
        _state.update { it.copy(connectionState = ConnectionState.CONNECTION_REQUEST) }
        deviceConnectionJob = bluetoothController.connectToDevice(device).observe()
    }

    fun disconnectDevice() {
        Log.d(tag, "disconnectDevice")
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(connectionState = ConnectionState.IDLE) }
    }

    fun sendMessage(message: String) {
        Log.d(tag, "sendMessage(): $message")
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message = message)
            if (bluetoothMessage != null) {
                _state.update {
                    it.copy(
                        messages = it.messages + bluetoothMessage
                    )
                }
            }
        }
    }

    fun sendImages(uri: Uri?) {
        Log.d(tag, "sendImages(): $uri")
        uri?.let { imageUri ->
            viewModelScope.launch {
                val bluetoothMessage = bluetoothController.trySendImage(uri = imageUri)
                if (bluetoothMessage != null) {
                    _state.update {
                        it.copy(
                            messages = it.messages + bluetoothMessage
                        )
                    }
                }
            }
        }
    }

    fun changeDeviceName(deviceName: String) {
        Log.d(tag, "changeDeviceName(): $deviceName")
        bluetoothController.changeDeviceName(deviceName = deviceName)
    }

    fun enableBluetooth() {
        Log.d(tag, "enableBluetooth()")
        bluetoothController.enableBluetooth()
    }

    fun disableBluetooth() {
        Log.d(tag, "disableBluetooth()")
        bluetoothController.disableBluetooth()
    }


    fun enableDiscoverability() {
        Log.d(tag, "enableDiscoverability()")
        bluetoothController.enableDiscoverability()
    }

    fun disableDiscoverability() {
        Log.d(tag, "disableDiscoverability()")
        bluetoothController.disableDiscoverability()
    }

    private fun Flow<ConnectionResult>.observe(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionRequest -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.CONNECTION_REQUEST,
                            errorMessage = null
                        )
                    }
                }

                ConnectionResult.ConnectionOpen -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.CONNECTION_OPEN, errorMessage = null
                        )
                    }

                }

                is ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.CONNECTION_ACTIVE,
                            errorMessage = null,
                            messages = result.messages
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.IDLE, errorMessage = result.message
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    _state.update {
                        it.copy(
                            messages = it.messages + result.message
                        )
                    }
                }
            }
        }.catch {
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    connectionState = ConnectionState.IDLE, errorMessage = null
                )
            }
        }.launchIn(viewModelScope)
    }

    fun clearErrorMessage() {
        _state.update {
            it.copy(
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}