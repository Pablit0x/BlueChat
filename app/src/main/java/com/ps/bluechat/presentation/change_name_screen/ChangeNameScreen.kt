package com.ps.bluechat.presentation.change_name_screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.navigation.Direction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNameScreen(
    modifier: Modifier = Modifier,
    direction: Direction,
    deviceName: String,
    onDeviceNameChange: (String) -> Unit
) {
    var updatedName by remember {
        mutableStateOf(deviceName)
    }


    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colors.background
        ), title = {
            Text(
                text = stringResource(id = R.string.rename_device),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(onClick = {
                direction.navigateBack()
            }, modifier = Modifier.padding(start = 16.dp)) {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = null
                )
            }
        }, actions = {
            IconButton(onClick = {
                onDeviceNameChange(updatedName)
                direction.navigateToHomeScreen()
            }, modifier = Modifier.padding(end = 16.dp)) {
                Icon(
                    imageVector = Icons.Default.Check, contentDescription = null
                )
            }
        })
    }) { padding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextField(value = updatedName,
                    onValueChange = { if (it.length <= 16) updatedName = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.onSecondary,
                        backgroundColor = MaterialTheme.colors.secondary,
                        cursorColor = MaterialTheme.colors.onSecondary
                    ),
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.other_nearby_devices_can_see_this_name),
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}