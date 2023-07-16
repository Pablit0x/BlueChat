package com.ps.bluechat.presentation.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState
import com.talhafaki.composablesweettoast.main.SweetToast
import com.talhafaki.composablesweettoast.util.SweetToastUtil.SweetError
import es.dmoral.toasty.Toasty


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceScreen(
    direction: Direction,
    state: BluetoothUiState,
    onStartConnecting: (BluetoothDeviceDomain) -> Unit,
    onStartServer: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDiscoverabilityEnable: () -> Unit,
    onDiscoverabilityDisable: () -> Unit,
    onBluetoothEnable: () -> Unit,
    onBluetoothDisable: () -> Unit

) {
    val context: Context = LocalContext.current

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { message ->
            Toasty.error(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    when (state.connectionState) {
        ConnectionState.CONNECTION_OPEN -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.Gray,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .progressSemantics()
                        .size(42.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = context.getString(
                        R.string.waiting_for_the_other_user_to_join
                    ), color = MaterialTheme.colors.onSurface, fontSize = 14.sp
                )
            }
        }
        ConnectionState.CONNECTION_REQUEST -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.Gray,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .progressSemantics()
                        .size(42.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = context.getString(
                        R.string.joining_the_chat
                    ), color = MaterialTheme.colors.onSurface, fontSize = 14.sp
                )
            }
        }

        ConnectionState.CONNECTION_ACTIVE -> {
            direction.navigateToChatScreen()
        }

        else -> {
            Scaffold(floatingActionButton = {
                BluetoothActionSelector(
                    scanningState = state.scanningState,
                    onStartScan = onStartScan,
                    onStopScan = onStopScan,
                    onStartServer = onStartServer
                )
            }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    ModeToggleField(
                        isOn = state.isBluetoothEnabled,
                        modeName = context.getString(com.ps.bluechat.R.string.bluetooth),
                        onEnable = onBluetoothEnable,
                        onDisable = onBluetoothDisable
                    )

                    ModeToggleField(
                        isOn = state.isDeviceDiscoverable,
                        modeName = context.getString(com.ps.bluechat.R.string.discoverability),
                        onEnable = onDiscoverabilityEnable,
                        onDisable = onDiscoverabilityDisable
                    )

                    DeviceNameField(
                        deviceName = state.deviceName
                            ?: context.getString(com.ps.bluechat.R.string.no_name),
                        direction = direction
                    )
                    Divider(color = Color.DarkGray, thickness = 1.dp)

                    BluetoothDeviceList(
                        scanningState = state.scanningState,
                        pairedDevices = state.pairedDevices,
                        scannedDevices = state.scannedDevices,
                        onStartConnecting = onStartConnecting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onRestartScan = onStartScan
                    )
                }
            }
        }

    }
}