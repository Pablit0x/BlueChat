package com.ps.bluechat.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionResult
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.util.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {
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
        Log.d(TAG, "startScan()")
        bluetoothController.startScanning()
    }

    fun createBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "createBond(): $device")
        bluetoothController.createBond(device = device)
    }

    fun removeBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "removeBond(): $device")
        bluetoothController.removeBond(device = device)
    }

    fun stopScan() {
        Log.d(TAG, "stopScan()")
        bluetoothController.stopScanning()
    }

    fun observeIncomingConnections() {
        Log.d(TAG, "observeIncomingConnections()")
        _state.update { it.copy(connectionState = ConnectionState.CONNECTION_OPEN) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().observe()
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        Log.d(TAG, "connectToDevice(): ${device.deviceName}")
        _state.update { it.copy(connectionState = ConnectionState.CONNECTION_REQUEST) }
        deviceConnectionJob = bluetoothController.connectToDevice(device).observe()
    }

    fun disconnectDevice() {
        Log.d(TAG, "disconnectDevice")
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(connectionState = ConnectionState.IDLE) }
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "sendMessage(): $message")
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
        Log.d(TAG, "sendImages(): $uri")
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
        Log.d(TAG, "changeDeviceName(): $deviceName")
        bluetoothController.changeDeviceName(deviceName = deviceName)
    }

    fun enableBluetooth() {
        Log.d(TAG, "enableBluetooth()")
        bluetoothController.enableBluetooth()
    }

    fun disableBluetooth() {
        Log.d(TAG, "disableBluetooth()")
        bluetoothController.disableBluetooth()
    }


    fun enableDiscoverability() {
        Log.d(TAG, "enableDiscoverability()")
        bluetoothController.enableDiscoverability()
    }

    fun disableDiscoverability() {
        Log.d(TAG, "disableDiscoverability()")
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
                    result.messages.collectLatest { messages ->
                        _state.update {
                            it.copy(
                                connectionState = ConnectionState.CONNECTION_ACTIVE,
                                errorMessage = null,
                                messages = messages
                            )
                        }
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.IDLE, errorMessage = result.message
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