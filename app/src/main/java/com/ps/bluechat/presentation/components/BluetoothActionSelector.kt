package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.ScanningState


@ExperimentalMaterialApi
@Composable
fun BluetoothActionSelector(
    modifier : Modifier = Modifier,
    scanningState: ScanningState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit
) {
    val context: Context = LocalContext.current
    var isMenuExtended by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(visible = isMenuExtended, modifier = Modifier.padding(bottom = 16.dp)) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {

                when (scanningState) {
                    ScanningState.DISCOVERING -> {
                        MenuActionButton(
                            icon = Icons.Default.Pause,
                            description = context.getString(R.string.stop_scan),
                            onClickAction = {
                                onStopScan()
                            },
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    else -> {
                        MenuActionButton(
                            icon = Icons.Default.PlayArrow,
                            description = context.getString(R.string.start_scan),
                            onClickAction = {
                                onStartScan()
                            },
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                }


                MenuActionButton(
                    icon = Icons.Default.BluetoothSearching,
                    description = context.getString(R.string.start_server),
                    onClickAction = {
                        onStartServer()
                        isMenuExtended = !isMenuExtended
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
        IconButton(onClick = { isMenuExtended = !isMenuExtended }) {
            Icon(
                imageVector = if (isMenuExtended) Icons.Default.KeyboardArrowDown else Icons.Default.Menu,
                contentDescription = "",
                tint = MaterialTheme.colors.onSecondary,
                modifier = Modifier
                    .size(54.dp)
                    .background(MaterialTheme.colors.secondary, shape = CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun MenuActionButton(
    icon: ImageVector, description: String, onClickAction: () -> Unit, modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        Text(text = description)

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colors.onSecondary,
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MaterialTheme.colors.secondary, shape = CircleShape
                )
                .padding(8.dp)
                .clickable { onClickAction() }
        )
    }
}
