package com.ps.bluechat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDeviceDomain>,
    scannedDevices: List<BluetoothDeviceDomain>,
    onClick: (BluetoothDeviceDomain) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(pairedDevices) { pairedDevice ->
            Text(
                text = pairedDevice.deviceName ?: "Unknown",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(pairedDevice) }
                    .padding(16.dp)
            )
        }

        item {
            Text(
                text = "Scanned Devices",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(scannedDevices) { scannedDevice ->
            Text(
                text = scannedDevice.deviceName ?: "Unknown",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(scannedDevice) }
                    .padding(16.dp)
            )
        }
    }
}