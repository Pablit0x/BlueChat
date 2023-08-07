package com.ps.bluechat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionResult
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.presentation.model.BluetoothState
import com.ps.bluechat.util.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private var _state = MutableStateFlow(BluetoothState())
    private var deviceConnectionJob: Job? = null

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
        combineState
    ) { isBluetoothEnabled, isDeviceDiscoverable, state ->
        state.copy(
            isBluetoothEnabled = isBluetoothEnabled,
            isDeviceDiscoverable = isDeviceDiscoverable,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

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

    fun startScan() {
        Log.d(TAG, "startScan()")
        bluetoothController.startScanning()
    }

    fun stopScan() {
        Log.d(TAG, "stopScan()")
        bluetoothController.stopScanning()
    }

    fun createBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "createBond(): $device")
        bluetoothController.createBond(device = device)
    }

    fun removeBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "removeBond(): $device")
        bluetoothController.removeBond(device = device)
    }

    fun observeIncomingConnections() {
        Log.d(TAG, "observeIncomingConnections()")
        _state.update { it.copy(connectionState = ConnectionState.OPEN) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().observe()
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        Log.d(TAG, "connectToDevice(): ${device.deviceName}")
        _state.update { it.copy(connectionState = ConnectionState.REQUEST) }
        deviceConnectionJob = bluetoothController.connectToDevice(device).observe()
    }

    fun disconnectDevice() {
        Log.d(TAG, "disconnectDevice")
        deviceConnectionJob?.cancel()
        _state.update { it.copy(connectionState = ConnectionState.IDLE) }
        bluetoothController.closeConnection()
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
                            connectionState = ConnectionState.REQUEST
                        )
                    }
                }

                ConnectionResult.ConnectionOpen -> {
                    _state.update {
                        it.copy(
                            connectionState = ConnectionState.OPEN
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
            }
        }.catch { throwable ->
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    connectionState = ConnectionState.IDLE, errorMessage = throwable.message
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