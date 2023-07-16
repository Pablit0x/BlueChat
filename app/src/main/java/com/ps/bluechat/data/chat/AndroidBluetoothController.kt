package com.ps.bluechat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.*
import com.ps.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val bluetoothDeviceReceiver = BluetoothDeviceReceiver(onDeviceFound = { foundDevice ->
        _scannedDevices.update { devices ->
            val newDevice = foundDevice.toBluetoothDeviceDomain()
            val existingDevice = devices.any {
                it.address == newDevice.address
            }
            val isDeviceAlreadyPaired = pairedDevices.value.any { it == newDevice }
            if (existingDevice || isDeviceAlreadyPaired) devices else devices + newDevice
        }
    }, onStateChanged = { connectionState, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _connectionState.update { connectionState }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.tryEmit(context.getString(R.string.cant_connect_to_not_paired_device))
            }
        }
    })


    private val bluetoothAdapterReceiver =
        BluetoothAdapterReceiver(isBluetoothEnabled = { isBluetoothEnabled ->
            _isBluetoothEnabled.update {
                isBluetoothEnabled
            }

        }, isDiscovering = { scanningState ->
            _scanningState.update { scanningState }
        }, isDeviceDiscoverable = { isDeviceDiscoverable ->
            _isDeviceDiscoverable.update { isDeviceDiscoverable }
        })

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null


    private val _deviceName = MutableStateFlow<String?>(null)
    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _isBluetoothEnabled = MutableStateFlow(false)
    private val _isDeviceDiscoverable = MutableStateFlow(false)
    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    private val _errors = MutableSharedFlow<String>()


    init {
        updatePairedDevices()
        updateInitialBluetoothState()
        updateDeviceName()
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

    override val isDeviceDiscoverable: StateFlow<Boolean>
        get() = _isDeviceDiscoverable.asStateFlow()

    override val connectionState: StateFlow<ConnectionState>
        get() = _connectionState.asStateFlow()

    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()


    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(context.getString(R.string.bluetooth_denied))
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                Constants.SERVICE_NAME, UUID.fromString(Constants.SERVICE_UUID)
            )

            emit(ConnectionResult.ConnectionOpen)
            currentClientSocket = currentServerSocket?.accept(10000)
            emit(ConnectionResult.ConnectionEstablished)
            currentClientSocket?.let {
                currentServerSocket?.close()
                val service = BluetoothDataTransferService(it)
                dataTransferService = service
                emitAll(service.listenForIncomingMessages().map { message ->
                    ConnectionResult.TransferSucceeded(message)
                })
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(context.getString(R.string.bluetooth_denied))
            }

            currentClientSocket = bluetoothAdapter?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(Constants.SERVICE_UUID)
                )

            stopScanning()

            emit(ConnectionResult.ConnectionRequest)
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(it.listenForIncomingMessages().map { message ->
                            ConnectionResult.TransferSucceeded(message)
                        })
                    }
                } catch (e: IOException) {
                    emit(ConnectionResult.Error(context.getString(R.string.connection_failed)))
                    try {
                        socket.close()
                        currentClientSocket = null
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return null

        if (dataTransferService == null) {
            return null
        }


        val bluetoothMessage = BluetoothMessage(
            message = message,
            isFromLocalUser = true,
            address = bluetoothAdapter?.address ?: "My Address",
            time = getCurrentTime()
        )

        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    override fun closeConnection() {
        currentServerSocket?.close()
        currentClientSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

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
            addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        })

    }

    override fun registerBluetoothDeviceReceiver() {
        context.registerReceiver(bluetoothDeviceReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_NAME_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            addAction(BluetoothDevice.ACTION_UUID)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
            addAction(BluetoothDevice.ACTION_ALIAS_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_CLASS_CHANGED)

        })

    }

    override fun startScanning() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        updatePairedDevices()
        _scannedDevices.update { emptyList() }
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
        closeConnection()
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter?.bondedDevices?.map {
            it.toBluetoothDeviceDomain()
        }?.also { pairedDevices ->
            _scannedDevices.update { scannedDevices ->
                scannedDevices.subtract(pairedDevices).toList()
            }
            _pairedDevices.update { pairedDevices }
        }
    }

    private fun updateInitialBluetoothState() {
        bluetoothAdapter?.isEnabled?.also { isEnabled -> _isBluetoothEnabled.update { isEnabled } }
    }

    override fun changeDeviceName(deviceName: String) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
            return
        }

        while (bluetoothAdapter?.name != deviceName) {
            bluetoothAdapter?.name = deviceName
        }

        _deviceName.update { deviceName }
    }

    override fun enableDiscoverability() {
        val enableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        enableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        enableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        context.startActivity(enableDiscoverabilityIntent)
    }

    override fun disableDiscoverability() {
        val disableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        disableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        disableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1)
        context.startActivity(disableDiscoverabilityIntent)
    }

    override fun updateDeviceName() {
        _deviceName.update { bluetoothAdapter?.name ?: context.getString(R.string.no_name) }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}