package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ps.bluechat.R
import com.ps.bluechat.navigation.Direction

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChangeNameScreen(
    modifier: Modifier = Modifier,
    direction: Direction,
    deviceName: String,
    onDeviceNameChange: (String) -> Unit
) {


    val context: Context = LocalContext.current
    var updatedName by remember {
        mutableStateOf(deviceName)
    }

    val focusRequester = remember { FocusRequester() }

    val softKeyboard = LocalSoftwareKeyboardController.current


    Column(
        modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { direction.navigateBackToHomeScreen() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = modifier.size(32.dp)
                )
            }
            Text(
                text = context.getString(R.string.rename_device),
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Monospace,
                fontSize = 22.sp,
            )
            IconButton(onClick = {
                onDeviceNameChange(updatedName)
                direction.navigateToHomeScreen()
            }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = modifier.size(32.dp)
                )
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = updatedName,
                onValueChange = { if(it.length <= 16) updatedName = it },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .focusRequester(focusRequester)
                    .onGloballyPositioned {
                        focusRequester.requestFocus()
                        softKeyboard?.show()
                    }
            )

            Text(
                text = stringResource(id = R.string.other_nearby_devices_can_see_this_name),
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                color = Color.DarkGray
            )

        }

    }
}