package com.ps.bluechat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ps.bluechat.presentation.BluetoothUiState


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceScreen(
    state : BluetoothUiState,
    onStartScan : () -> Unit,
    onStopScan : () -> Unit
){
    Scaffold(
        floatingActionButton = {
            BluetoothActionSelector(
                onStartScan = {onStartScan()},
                onStopScan = {onStopScan()},
            onStartServer = {})
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BluetoothDeviceList(pairedDevices = state.pairedDevices, scannedDevices = state.scannedDevices, onClick = {},
            modifier = Modifier.fillMaxWidth().weight(1f))
        }
    }
}