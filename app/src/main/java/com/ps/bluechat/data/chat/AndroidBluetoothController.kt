package com.ps.bluechat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.*
import com.ps.bluechat.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


@SuppressLint("MissingPermission", "HardwareIds")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val tag = AndroidBluetoothController::class.simpleName

    private val bluetoothDeviceReceiver = BluetoothDeviceReceiver(onDeviceFound = { foundDevice ->
        _scannedDevices.update { devices ->
            val newDevice = foundDevice.toBluetoothDeviceDomain()
            val existingDevice = devices.any { it.address == newDevice.address }
            val isDeviceAlreadyPaired =
                bluetoothAdapter?.bondedDevices?.any { it.address == newDevice.address } ?: false
            if (existingDevice || isDeviceAlreadyPaired) devices else devices + newDevice
        }
    }, onStateChanged = { connectionState, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _connectionState.update { connectionState }
            when (connectionState) {
                ConnectionState.CONNECTION_ACTIVE -> _connectedDevice.update { bluetoothDevice.toBluetoothDeviceDomain() }
                ConnectionState.IDLE -> _connectedDevice.update { null }
                else -> {}
            }
        }
    }, onBondStateChanged = { updatePairedDevices() }
    )


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
    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(mutableListOf())
    private val _isBluetoothEnabled = MutableStateFlow(false)
    private val _isDeviceDiscoverable = MutableStateFlow(false)
    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    private val _errors = MutableStateFlow<String?>(null)


    init {
        updatePairedDevices()
        updateInitialBluetoothState()
        updateDeviceName()
    }

    override val deviceName: StateFlow<String?>
        get() = _deviceName.asStateFlow()

    override val connectedDevice: StateFlow<BluetoothDeviceDomain?>
        get() = _connectedDevice.asStateFlow()

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

    override val errors: StateFlow<String?>
        get() = _errors.asStateFlow()


    override fun startBluetoothServer(): Flow<ConnectionResult> {
        Log.d(tag, "startBluetoothServer()")
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(context.getString(R.string.bluetooth_denied))
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                Constants.SERVICE_NAME, UUID.fromString(Constants.SERVICE_UUID)
            )

            emit(ConnectionResult.ConnectionOpen)
            currentClientSocket = currentServerSocket?.accept()
            emit(ConnectionResult.ConnectionEstablished)

            currentClientSocket?.let {
                currentServerSocket?.close()
                val service = BluetoothDataTransferService(it)
                dataTransferService = service
                emitAll(service.listenForIncomingMessages(context = context).map { message ->
                    ConnectionResult.TransferSucceeded(message)
                })
            }

        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        Log.d(tag, "connectToDevice(): $device")
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
            var isSuccess = false
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    isSuccess = true
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(it.listenForIncomingMessages(context = context).map { message ->
                            ConnectionResult.TransferSucceeded(message)
                        })
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    if(!isSuccess)
                        emit(ConnectionResult.Error(context.getString(R.string.connection_failed)))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        Log.d(tag, "trySendMessage(): $message")
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

    private val CHUNK_SIZE = 2048 // Set an appropriate chunk size

    override suspend fun trySendImage(uri: Uri): BluetoothMessage? {
        Log.d(tag, "trySendImage(): $uri")
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return null

        if (dataTransferService == null) {
            return null
        }

        val desiredWidth = 300
        val desiredHeight = 300
        val quality = 15

        // Convert the image URI to a Bitmap
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))

        // Downscale the bitmap to desired dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true)

        // Compress the scaled bitmap and convert it to a byte array
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Get the complete byte array of the image
        val completeByteArray = outputStream.toByteArray()

        // Send the image size first to prepare the receiver for incoming data
        dataTransferService?.sendMessage(completeByteArray.size.toString().toByteArray())

        // Send the image in chunks
        var offset = 0
        while (offset < completeByteArray.size) {
            val chunkSize = CHUNK_SIZE.coerceAtMost(completeByteArray.size - offset)
            val chunk = completeByteArray.copyOfRange(offset, offset + chunkSize)
            dataTransferService?.sendMessage(chunk)
            offset += chunkSize
        }

        return BluetoothMessage(
            imageUri = uri,
            isFromLocalUser = true,
            address = bluetoothAdapter?.address ?: "My Address",
            time = getCurrentTime()
        )
    }

    override fun closeConnection() {
        Log.d(tag, "closeConnection()")
        currentServerSocket?.close()
        currentClientSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    @Suppress("DEPRECATION")
    override fun enableBluetooth() {
        Log.d(tag, "enableBluetooth()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            bluetoothAdapter?.enable()
        }
    }

    @Suppress("DEPRECATION")
    override fun disableBluetooth() {
        Log.d(tag, "disableBluetooth()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent("android.bluetooth.adapter.action.REQUEST_DISABLE")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            bluetoothAdapter?.disable()
        }
    }

    override fun registerBluetoothAdapterReceiver() {
        Log.d(tag, "registerBluetoothAdapterReceiver()")
        context.registerReceiver(bluetoothAdapterReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        })

    }

    override fun registerBluetoothDeviceReceiver() {
        Log.d(tag, "registerBluetoothDeviceReceiver()")
        context.registerReceiver(bluetoothDeviceReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

        })

    }

    override fun startScanning() {
        Log.d(tag, "startScanning()")
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        _scannedDevices.update { emptyList() }
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopScanning() {
        Log.d(tag, "stopScanning()")
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        Log.d(tag, "release()")
        context.unregisterReceiver(bluetoothDeviceReceiver)
        context.unregisterReceiver(bluetoothAdapterReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        Log.d(tag, "updatePairedDevices()")
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        bluetoothAdapter?.bondedDevices?.map {
            it.toBluetoothDeviceDomain()
        }?.also { pairedDevices ->
            _pairedDevices.update { pairedDevices }
            _scannedDevices.update { scannedDevices ->
                scannedDevices.filter { !pairedDevices.contains(it) }
            }
        }
    }

    private fun updateInitialBluetoothState() {
        bluetoothAdapter?.isEnabled?.also { isEnabled -> _isBluetoothEnabled.update { isEnabled } }
    }

    override fun changeDeviceName(deviceName: String) {
        Log.d(tag,"changeDeviceName(): $deviceName")

        while (bluetoothAdapter?.name != deviceName) {
            bluetoothAdapter?.name = deviceName
        }
        _deviceName.update { deviceName }
    }

    override fun enableDiscoverability() {
        Log.d(tag, "enableDiscoverability()")
        val enableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        enableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        enableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        context.startActivity(enableDiscoverabilityIntent)
    }

    override fun disableDiscoverability() {
        Log.d(tag, "disableDiscoverability()")
        val disableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        disableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        disableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1)
        context.startActivity(disableDiscoverabilityIntent)
    }

    override fun updateDeviceName() {
        val name = bluetoothAdapter?.name
        Log.d(tag, "updateDeviceName(): $name")

        _deviceName.update { name ?: context.getString(R.string.no_name) }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun removeBond(device: BluetoothDeviceDomain) {
        Log.d(tag, "removeBond(): $device")
        try {
            val deviceToRemove = bluetoothAdapter?.getRemoteDevice(device.address)
            deviceToRemove?.let {
                deviceToRemove::class.java.getMethod("removeBond").invoke(deviceToRemove)
            }
        } catch (e: Exception) {
            Log.e(tag, "Removing bond has been failed. ${e.message}")
        }
    }

    override fun createBond(device: BluetoothDeviceDomain) {
        Log.d(tag, "createBond(): $device")
        try {
            val deviceToBond = bluetoothAdapter?.getRemoteDevice(device.address)
            deviceToBond?.let {
                deviceToBond.createBond()
            }
        } catch (e: Exception) {
            Log.e(tag, "Creating bond has been failed. ${e.message}")
        }
    }

}