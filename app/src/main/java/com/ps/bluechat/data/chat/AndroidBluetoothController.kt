package com.ps.bluechat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
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
import com.ps.bluechat.domain.repository.ChatRepository
import com.ps.bluechat.util.Constants
import com.ps.bluechat.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


@SuppressLint("MissingPermission", "HardwareIds")
class AndroidBluetoothController(
    private val context: Context, private val chatRepository: ChatRepository
) : BluetoothController {

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
                ConnectionState.ACTIVE -> _connectedDevice.update { bluetoothDevice.toBluetoothDeviceDomain() }
                ConnectionState.IDLE -> _connectedDevice.update { null }
                else -> {}
            }
        }
    }, onBondStateChanged = { updatePairedDevices() })


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

    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    private val _deviceName = MutableStateFlow<String?>(null)
    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    private val _scanningState = MutableStateFlow(ScanningState.IDLE)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(mutableListOf())
    private val _isBluetoothEnabled = MutableStateFlow(false)
    private val _isDeviceDiscoverable = MutableStateFlow(false)
    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    private val _messages = MutableStateFlow<List<BluetoothMessage>>(emptyList())
    private val _errors = MutableSharedFlow<String?>()


    init {
        getAllMessages()
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

    override val messages: StateFlow<List<BluetoothMessage>>
        get() = _messages.asStateFlow()

    override val errors: SharedFlow<String?>
        get() = _errors.asSharedFlow()

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        Log.d(TAG, "startBluetoothServer()")
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(context.getString(R.string.bluetooth_denied))
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                Constants.SERVICE_NAME, UUID.fromString(Constants.SERVICE_UUID)
            )

            emit(ConnectionResult.ConnectionOpen)

            currentClientSocket = currentServerSocket?.accept(10000)
            currentClientSocket?.let { socket ->
                currentServerSocket?.close()
                val service = BluetoothDataTransferService(socket = socket)
                dataTransferService = service

                service.listenForIncomingMessages(context = context).collect { message ->
                    chatRepository.insertMessage(message)
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        Log.d(TAG, "connectToDevice(): $device")
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException(context.getString(R.string.bluetooth_denied))
            }


            stopScanning()

            currentClientSocket?.close()
            currentClientSocket = bluetoothAdapter?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(Constants.SERVICE_UUID)
                )

            emit(ConnectionResult.ConnectionRequest)

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    BluetoothDataTransferService(socket = socket).also { service ->
                        dataTransferService = service
                        service.listenForIncomingMessages(context = context).collect { message ->
                            chatRepository.insertMessage(message)
                        }
                    }

                } catch (e: IOException) {
                    emit(ConnectionResult.Error(context.getString(R.string.connection_failed)))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }


    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        Log.d(TAG, "trySendMessage(): $message")
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return null

        if (dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            isFromLocalUser = true,
            address = currentClientSocket?.remoteDevice?.address
                ?: context.getString(R.string.unknown),
            time = getCurrentTime()
        )

        if (dataTransferService?.sendMessage(bluetoothMessage.toByteArray()) == true) {
            chatRepository.insertMessage(bluetoothMessage = bluetoothMessage)
        }

        return bluetoothMessage
    }

    override suspend fun trySendImage(uri: Uri): BluetoothMessage? {
        Log.d(TAG, "trySendImage(): $uri")
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return null

        if (dataTransferService == null) {
            return null
        }

        val desiredChunkSize = 1024
        val desiredWidth = 500
        val desiredHeight = 500
        val quality = 15

        // Convert the image URI to a Bitmap
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))

        // Downscale the bitmap to desired dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true)

        val savedUri =
            dataTransferService?.saveBitmapToMediaStore(context = context, bitmap = scaledBitmap)

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
            val chunkSize = desiredChunkSize.coerceAtMost(completeByteArray.size - offset)
            val chunk = completeByteArray.copyOfRange(offset, offset + chunkSize)
            dataTransferService?.sendMessage(chunk)
            offset += chunkSize
        }

        val bluetoothMessage = BluetoothMessage(
            imageUri = savedUri,
            isFromLocalUser = true,
            address = currentClientSocket?.remoteDevice?.address
                ?: context.getString(R.string.unknown),
            time = getCurrentTime()
        )

        chatRepository.insertMessage(bluetoothMessage = bluetoothMessage)

        return bluetoothMessage
    }

    override fun closeConnection() {
        Log.d(TAG, "closeConnection()")
        currentServerSocket?.close()
        currentClientSocket?.close()
        currentClientSocket = null
        currentServerSocket = null

    }

    @Suppress("DEPRECATION")
    override fun enableBluetooth() {
        Log.d(TAG, "enableBluetooth()")
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
        Log.d(TAG, "disableBluetooth()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent("android.bluetooth.adapter.action.REQUEST_DISABLE")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            bluetoothAdapter?.disable()
        }
    }

    override fun registerBluetoothAdapterReceiver() {
        Log.d(TAG, "registerBluetoothAdapterReceiver()")
        context.registerReceiver(bluetoothAdapterReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        })

    }

    override fun registerBluetoothDeviceReceiver() {
        Log.d(TAG, "registerBluetoothDeviceReceiver()")
        context.registerReceiver(bluetoothDeviceReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

        })

    }

    override fun startScanning() {
        Log.d(TAG, "startScanning()")
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        _scannedDevices.update { emptyList() }
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopScanning() {
        Log.d(TAG, "stopScanning()")
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        Log.d(TAG, "release()")
        closeConnection()
        context.unregisterReceiver(bluetoothDeviceReceiver)
        context.unregisterReceiver(bluetoothAdapterReceiver)
    }

    private fun updatePairedDevices() {
        Log.d(TAG, "updatePairedDevices()")
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

    private fun getAllMessages() {
        coroutineScope.launch {
            chatRepository.getAllMessages().collect { messages ->
                _messages.update {
                    messages
                }
            }
        }
    }

    override fun changeDeviceName(deviceName: String) {
        Log.d(TAG, "changeDeviceName(): $deviceName")

        while (bluetoothAdapter?.name != deviceName) {
            bluetoothAdapter?.name = deviceName
        }
        _deviceName.update { deviceName }
    }

    override fun enableDiscoverability() {
        Log.d(TAG, "enableDiscoverability()")
        val enableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        enableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        enableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        context.startActivity(enableDiscoverabilityIntent)
    }

    override fun disableDiscoverability() {
        Log.d(TAG, "disableDiscoverability()")
        val disableDiscoverabilityIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        disableDiscoverabilityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        disableDiscoverabilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1)
        context.startActivity(disableDiscoverabilityIntent)
    }

    override fun updateDeviceName() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        val name = bluetoothAdapter?.name
        Log.d(TAG, "updateDeviceName(): $name")

        _deviceName.update { name ?: context.getString(R.string.no_name) }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun removeBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "removeBond(): $device")
        try {
            val deviceToRemove = bluetoothAdapter?.getRemoteDevice(device.address)
            deviceToRemove?.let {
                deviceToRemove::class.java.getMethod("removeBond").invoke(deviceToRemove)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Removing bond has been failed. ${e.message}")
        }
    }

    override fun createBond(device: BluetoothDeviceDomain) {
        Log.d(TAG, "createBond(): $device")
        try {
            val deviceToBond = bluetoothAdapter?.getRemoteDevice(device.address)
            deviceToBond?.let {
                deviceToBond.createBond()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Creating bond has been failed. ${e.message}")
        }
    }

}