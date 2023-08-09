package com.ps.bluechat.navigation

import androidx.navigation.NavHostController

class Direction(
    navController: NavHostController
) {
    val navigateToHomeScreen: () -> Unit = {
        navController.navigate(Screen.HomeScreen.route)
    }

    val navigateBackToHomeScreen: () -> Unit = {
        navController.navigate(Screen.HomeScreen.route) {
            popUpTo(Screen.HomeScreen.route) {
                inclusive = true
            }
        }
    }

    val navigateToChangeDeviceNameScreen: (deviceName: String) -> Unit = { deviceName ->
        navController.navigate("${Screen.ChangeDeviceNameScreen.route}/$deviceName")
    }

    val navigateBack: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToChatScreen: () -> Unit = {
        navController.navigate(Screen.ChatScreen.route)
    }
}