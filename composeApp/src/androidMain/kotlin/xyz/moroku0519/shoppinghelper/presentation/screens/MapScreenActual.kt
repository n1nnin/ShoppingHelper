package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.runtime.Composable
import xyz.moroku0519.shoppinghelper.presentation.components.LocationPermissionState
import xyz.moroku0519.shoppinghelper.presentation.components.rememberAndroidLocationPermissionState

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState? {
    return rememberAndroidLocationPermissionState()
}