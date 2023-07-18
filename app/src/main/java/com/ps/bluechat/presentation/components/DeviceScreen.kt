package com.ps.bluechat.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ConnectionResult
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState
import com.ps.bluechat.presentation.BluetoothViewModel
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.flow


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceScreen(
    direction: Direction,
    state: BluetoothUiState,
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

    var isPopupDisplayed by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }
    var connectionState by remember { mutableStateOf(ConnectionState.IDLE) }

    var fabHeight by remember {
        mutableStateOf(0)
    }

    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { message ->
            popupMessage = message
            isPopupDisplayed = true
        }
    }

    LaunchedEffect(key1 = state.connectionState) {
        connectionState = when (state.connectionState) {
            ConnectionState.CONNECTION_OPEN -> ConnectionState.CONNECTION_OPEN
            ConnectionState.CONNECTION_REQUEST -> ConnectionState.CONNECTION_REQUEST
            ConnectionState.CONNECTION_ACTIVE -> ConnectionState.CONNECTION_ACTIVE
            else -> ConnectionState.IDLE
        }
    }

    when (connectionState) {
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
                    text = stringResource(
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
                    text = stringResource(
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
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    },
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


                if (isPopupDisplayed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = heightInDp + 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        ShowCustomPopupMessage(message = popupMessage,
                            isError = true,
                            duration = 2500,
                            onStopDisplaying = {
                                isPopupDisplayed = it
                                clearErrorMessage()
                            })
                    }
                }
            }
        }

    }
}