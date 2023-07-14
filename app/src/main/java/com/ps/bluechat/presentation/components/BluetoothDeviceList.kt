package com.ps.bluechat.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.ScanningState
import kotlinx.coroutines.delay

@Composable
fun BluetoothDeviceList(
    scanningState: ScanningState,
    pairedDevices: List<BluetoothDeviceDomain>,
    scannedDevices: List<BluetoothDeviceDomain>,
    onClick: (BluetoothDeviceDomain) -> Unit,
    onRestartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current

    LazyColumn(modifier = modifier) {
        item {
            if (pairedDevices.isNotEmpty()) {
                Text(
                    text = context.getString(R.string.paired_devices),
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(pairedDevices) { pairedDevice ->
            Text(text = pairedDevice.deviceName ?: context.getString(R.string.no_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(pairedDevice) }
                    .padding(16.dp))
        }

        item {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = context.getString(R.string.available_devices),
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp
                )

                when (scanningState) {
                    ScanningState.DISCOVERING -> {
                        CircularProgressIndicator(
                            color = Color.Gray, modifier = Modifier
                                .progressSemantics()
                                .size(32.dp)
                        )
                    }
                    ScanningState.NOT_DISCOVERING -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp).clickable { onRestartScan() }
                        )
                    }
                    else -> {}
                }
            }
        }
        items(scannedDevices) { scannedDevice ->
            Text(text = scannedDevice.deviceName ?: context.getString(R.string.no_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(scannedDevice) }
                    .padding(16.dp))
        }
    }
}