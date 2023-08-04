package com.ps.bluechat.navigation

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.ps.bluechat.R
import com.ps.bluechat.presentation.BluetoothViewModel
import com.ps.bluechat.presentation.components.ChangeNameScreen
import com.ps.bluechat.presentation.components.ChatScreen
import com.ps.bluechat.presentation.components.DeviceScreen

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
fun NavGraph(
    navController: NavHostController
) {
    val direction = remember(navController) { Direction(navController) }
    val context: Context = LocalContext.current
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state by viewModel.state.collectAsState()

    AnimatedNavHost(navController = navController,
        startDestination = Screen.HomeScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(
            route = Screen.HomeScreen.route
        ) {
            DeviceScreen(
                direction = direction,
                state = state,
                onCreateBond = viewModel::createBond,
                onRemoveBond = viewModel::removeBond,
                onStartScan = viewModel::startScan,
                onStopScan = viewModel::stopScan,
                onBluetoothEnable = viewModel::enableBluetooth,
                onBluetoothDisable = viewModel::disableBluetooth,
                onDiscoverabilityEnable = viewModel::enableDiscoverability,
                onDiscoverabilityDisable = viewModel::disableDiscoverability,
                onStartConnecting = viewModel::connectToDevice,
                onStartServer = viewModel::observeIncomingConnections,
                clearErrorMessage = viewModel::clearErrorMessage
            )
        }
        composable(
            route = Screen.ChangeDeviceNameScreen.route
        ) {
            ChangeNameScreen(direction = direction,
                deviceName = state.deviceName ?: context.getString(R.string.no_name),
                onDeviceNameChange = { newName -> viewModel.changeDeviceName(deviceName = newName) })
        }

        composable(
            route = Screen.ChatScreen.route
        ) {
            ChatScreen(
                direction = direction,
                state = state,
                onDisconnect = viewModel::disconnectDevice,
                onSendMessage = viewModel::sendMessage,
                onUriSelected = viewModel::sendImages,
                onDeleteAllMessages = viewModel::clearAllMessages
            )
        }
    }

}