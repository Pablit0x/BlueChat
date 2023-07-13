package com.ps.bluechat.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ps.bluechat.presentation.components.DeviceScreen
import com.ps.bluechat.ui.theme.BlueChatTheme
import com.ps.bluechat.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled : Boolean
        get() = bluetoothAdapter?.isEnabled == true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){/**/}

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val canEnableBluetooth = permissions[Constants.BLUETOOTH_CONNECT_PERMISSION] == true
            if(canEnableBluetooth && !isBluetoothEnabled){
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        permissionLauncher.launch(
            arrayOf(
                Constants.BLUETOOTH_SCAN_PERMISSION,
                Constants.BLUETOOTH_CONNECT_PERMISSION
            )
        )


        setContent {
            BlueChatTheme {

                val viewModel = hiltViewModel<BluetoothViewModel>()
                val state by viewModel.state.collectAsState()

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DeviceScreen(
                        state = state,
                        onStartScan = viewModel::startScan,
                        onStopScan = viewModel::stopScan
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BlueChatTheme {
        Greeting("Android")
    }
}