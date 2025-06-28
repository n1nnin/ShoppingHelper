package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import xyz.moroku0519.shoppinghelper.geofence.GeofenceIntegration
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

@Composable
actual fun GeofenceHandler(shops: List<ShopUi>) {
    val context = LocalContext.current
    
    LaunchedEffect(shops) {
        // Only setup geofences for shops that have pending items
        val shopsWithPendingItems = shops.filter { it.pendingItemsCount > 0 }
        if (shopsWithPendingItems.isNotEmpty()) {
            GeofenceIntegration.setupGeofences(context, shopsWithPendingItems)
        }
    }
}