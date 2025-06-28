package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.moroku0519.shoppinghelper.geofence.GeofenceIntegration

@Composable
actual fun GeofenceTestButton(
    modifier: Modifier
) {
    val context = LocalContext.current
    
    ExtendedFloatingActionButton(
        onClick = {
            GeofenceIntegration.testNotification(context)
        },
        modifier = modifier.padding(16.dp),
        icon = {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "通知テスト"
            )
        },
        text = {
            Text("通知テスト")
        }
    )
}