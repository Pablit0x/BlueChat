package com.ps.bluechat.navigation

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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.ps.bluechat.R
import com.ps.bluechat.presentation.BluetoothViewModel
import com.ps.bluechat.presentation.change_name_screen.ChangeNameScreen
import com.ps.bluechat.presentation.change_name_screen.ChangeNameViewModel
import com.ps.bluechat.presentation.chat_screen.ChatScreen
import com.ps.bluechat.presentation.chat_screen.ChatViewModel
import com.ps.bluechat.presentation.device_screen.DeviceScreen
import com.ps.bluechat.util.Constants

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
fun NavGraph(
    navController: NavHostController
) {
    val direction = remember(navController) { Direction(navController) }
    val sharedViewModel = hiltViewModel<BluetoothViewModel>()

    AnimatedNavHost(navController = navController,
        startDestination = Screen.HomeScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(
            route = Screen.HomeScreen.route
        ) {

            val state by sharedViewModel.state.collectAsState()

            DeviceScreen(
                direction = direction,
                state = state,
                onCreateBond = sharedViewModel::createBond,
                onRemoveBond = sharedViewModel::removeBond,
                onStartScan = sharedViewModel::startScan,
                onStopScan = sharedViewModel::stopScan,
                onBluetoothEnable = sharedViewModel::enableBluetooth,
                onBluetoothDisable = sharedViewModel::disableBluetooth,
                onDiscoverabilityEnable = sharedViewModel::enableDiscoverability,
                onDiscoverabilityDisable = sharedViewModel::disableDiscoverability,
                onStartConnecting = sharedViewModel::connectToDevice,
                onStartServer = sharedViewModel::observeIncomingConnections
            )
        }
        composable(
            route = "${Screen.ChangeDeviceNameScreen.route}/{${Constants.DEVICE_NAME_NAV_ARGUMENT}}",
            arguments = listOf(navArgument(
                Constants.DEVICE_NAME_NAV_ARGUMENT
            ) {
                type = NavType.StringType
            })
        ) { entry ->
            val changeNameViewModel = hiltViewModel<ChangeNameViewModel>()
            val deviceName = entry.arguments?.getString(Constants.DEVICE_NAME_NAV_ARGUMENT)
                ?: stringResource(id = R.string.unknown)

            ChangeNameScreen(direction = direction, deviceName = deviceName, onDeviceNameChange = {
                changeNameViewModel.changeDeviceName(deviceName = it)
            })
        }

        composable(
            route = Screen.ChatScreen.route
        ) {

            val chatViewModel = hiltViewModel<ChatViewModel>()
            val chatState by chatViewModel.state.collectAsState()

            ChatScreen(
                direction = direction,
                state = chatState,
                onSendMessage = chatViewModel::sendMessage,
                onUriSelected = chatViewModel::sendImages,
                onDeleteAllMessages = chatViewModel::clearAllMessages,
                onDisconnect = sharedViewModel::disconnectDevice
            )
        }
    }

}