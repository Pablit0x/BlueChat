package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceScreen(
    direction: Direction,
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onBluetoothEnable: () -> Unit,
    onBluetoothDisable: () -> Unit
) {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> onStartScan()
                Lifecycle.Event.ON_PAUSE -> onStopScan()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }

    }

    Scaffold(floatingActionButton = {
        BluetoothActionSelector(onStartScan = { onStartScan() },
            onStopScan = { onStopScan() },
            onStartServer = {})
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BluetoothToggleField(
                isBluetoothOn = state.isBluetoothEnabled,
                onBluetoothEnable = onBluetoothEnable,
                onBluetoothDisable = onBluetoothDisable
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
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onRestartScan = onStartScan
            )
        }
    }
}