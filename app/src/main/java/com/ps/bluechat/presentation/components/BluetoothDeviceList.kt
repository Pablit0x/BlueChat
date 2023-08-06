package com.ps.bluechat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ScanningState

@Composable
fun BluetoothDeviceList(
    scanningState: ScanningState,
    pairedDevices: List<BluetoothDeviceDomain>,
    scannedDevices: List<BluetoothDeviceDomain>,
    onStartConnecting: (BluetoothDeviceDomain) -> Unit,
    onCreateBond: (BluetoothDeviceDomain) -> Unit,
    onRemoveBond: (BluetoothDeviceDomain) -> Unit,
    onRestartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            if (pairedDevices.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.paired_devices),
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(pairedDevices) { pairedDevice ->
            BluetoothPairedDeviceItem(
                deviceName = pairedDevice.deviceName,
                onClick = {
                    onStartConnecting(pairedDevice)
                },
                onRemoveBond = { onRemoveBond(pairedDevice) }
            )
        }

        item {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(id = R.string.available_devices),
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp
                )

                when (scanningState) {
                    ScanningState.DISCOVERING -> {
                        CircularProgressIndicator(
                            color = Color.Gray, modifier = Modifier
                                .progressSemantics()
                                .size(24.dp)
                        )
                    }
                    ScanningState.NOT_DISCOVERING -> {
                        Icon(imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onRestartScan() })
                    }
                    else -> {}
                }
            }
        }
        items(scannedDevices) { scannedDevice ->
            BluetoothAvailableDeviceItem(
                deviceName = scannedDevice.deviceName ?: stringResource(id = R.string.no_name),
                onClick = { onCreateBond(scannedDevice) },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}