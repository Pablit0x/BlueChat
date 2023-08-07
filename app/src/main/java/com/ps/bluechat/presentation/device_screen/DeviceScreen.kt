package com.ps.bluechat.presentation.device_screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.components.BluetoothActionSelector
import com.ps.bluechat.presentation.components.BluetoothDeviceList
import com.ps.bluechat.presentation.components.DeviceNameField
import com.ps.bluechat.presentation.components.ModeToggleField
import com.ps.bluechat.presentation.model.BluetoothState
import com.ps.bluechat.presentation.model.ToastState
import com.talhafaki.composablesweettoast.util.SweetToastUtil.SweetError


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceScreen(
    direction: Direction,
    state: BluetoothState,
    onCreateBond: (BluetoothDeviceDomain) -> Unit,
    onRemoveBond: (BluetoothDeviceDomain) -> Unit,
    onStartConnecting: (BluetoothDeviceDomain) -> Unit,
    onStartServer: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDiscoverabilityEnable: () -> Unit,
    onDiscoverabilityDisable: () -> Unit,
    onBluetoothEnable: () -> Unit,
    onBluetoothDisable: () -> Unit,
    clearErrorMessage: () -> Unit
) {

    var toastState by remember { mutableStateOf(ToastState()) }

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { message ->
            toastState = toastState.copy(
                message = message,
                isDisplayed = true,
                isWarning = true
            )
            clearErrorMessage()
        }
    }

    when (state.connectionState) {
        ConnectionState.OPEN -> {
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
                    text = stringResource(
                        R.string.waiting_for_the_other_user_to_join
                    ), color = MaterialTheme.colors.onSurface, fontSize = 14.sp
                )
            }
        }

        ConnectionState.REQUEST -> {
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
                    text = stringResource(
                        R.string.joining_the_chat
                    ), color = MaterialTheme.colors.onSurface, fontSize = 14.sp
                )
            }
        }

        ConnectionState.ACTIVE -> {
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

                    if(toastState.isDisplayed){
                        SweetError(
                            message = toastState.message,
                            duration = Toast.LENGTH_SHORT,
                            padding = PaddingValues(16.dp),
                            contentAlignment = Alignment.Center
                        )
                        toastState.isDisplayed = false
                    }

                    ModeToggleField(
                        isOn = state.isBluetoothEnabled,
                        modeName = stringResource(R.string.bluetooth),
                        onEnable = onBluetoothEnable,
                        onDisable = onBluetoothDisable
                    )

                    ModeToggleField(
                        isOn = state.isDeviceDiscoverable,
                        modeName = stringResource(R.string.discoverability),
                        onEnable = onDiscoverabilityEnable,
                        onDisable = onDiscoverabilityDisable
                    )

                    DeviceNameField(
                        deviceName = state.deviceName ?: stringResource(R.string.no_name),
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
                        onRestartScan = onStartScan,
                        onCreateBond = onCreateBond,
                        onRemoveBond = onRemoveBond
                    )
                }
            }
        }
    }
}