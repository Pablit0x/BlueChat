package com.ps.bluechat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ScanningState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val bluetoothDeviceReceiver = BluetoothDeviceReceiver { foundDevice ->
        _scannedDevices.update { devices ->
            val newDevice = foundDevice.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothAdapterReceiver =
        BluetoothAdapterReceiver(isBluetoothEnabled = { isBluetoothEnabled ->
            _isBluetoothEnabled.update {
                isBluetoothEnabled
            }

        }, isDiscovering = { scanningState ->
            _scanningState.update { scanningState }
        })

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }


    private val _deviceName = MutableStateFlow<String?>(null)
    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _isBluetoothEnabled = MutableStateFlow(false)

    init {
        updatePairedDevices()
        updateDeviceName()
        updateInitialBluetoothState()
    }

    override val deviceName: StateFlow<String?>
        get() = _deviceName.asStateFlow()

    override val scanningState: StateFlow<ScanningState>
        get() = _scanningState.asStateFlow()

    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    override val isBluetoothEnabled: StateFlow<Boolean>
        get() = _isBluetoothEnabled.asStateFlow()

    override fun enableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            bluetoothAdapter?.enable()
        }
    }

    override fun disableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            bluetoothAdapter?.disable()
        }
    }

    override fun registerBluetoothAdapterReceiver() {
        context.registerReceiver(bluetoothAdapterReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })
    }

    override fun registerBluetoothDeviceReceiver() {
        context.registerReceiver(bluetoothDeviceReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_NAME_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
        })
    }

    override fun startScanning() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopScanning() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(bluetoothDeviceReceiver)
        context.unregisterReceiver(bluetoothAdapterReceiver)
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter?.bondedDevices?.map {
            it.toBluetoothDeviceDomain()
        }?.also { pairedDevices ->
            _scannedDevices.update { scannedDevices ->
                scannedDevices.filterNot { scannedDevice ->
                    _pairedDevices.value.contains(
                        scannedDevice
                    )
                }
            }
            _pairedDevices.update { pairedDevices }
        }
    }

    private fun updateDeviceName() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
            return
        }
        bluetoothAdapter?.name?.also { name -> _deviceName.update { name } }
    }

    private fun updateInitialBluetoothState(){
        bluetoothAdapter?.isEnabled?.also { isEnabled -> _isBluetoothEnabled.update { isEnabled } }
    }

    override fun changeDeviceName(newName: String) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
            return
        }
        bluetoothAdapter?.name = newName

        updateDeviceName()
    }


    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}