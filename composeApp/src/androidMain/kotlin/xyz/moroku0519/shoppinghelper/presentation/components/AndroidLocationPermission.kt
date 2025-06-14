package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

// Android固有の権限実装
@OptIn(ExperimentalPermissionsApi::class)
class AndroidLocationPermissionState(
    private val permissionState: PermissionState
) : LocationPermissionState {
    override val isGranted: Boolean
        get() = permissionState.status.isGranted

    override val shouldShowRationale: Boolean
        get() = permissionState.status.shouldShowRationale

    override fun requestPermission() {
        permissionState.launchPermissionRequest()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAndroidLocationPermissionState(): LocationPermissionState {
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    return remember(permissionState) {
        AndroidLocationPermissionState(permissionState)
    }
}
