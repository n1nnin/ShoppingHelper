package xyz.moroku0519.shoppinghelper.geofence

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

object GeofenceIntegration {
    
    fun setupGeofences(context: Context, shops: List<ShopUi>) {
        val geofenceManager = GeofenceManager(context)
        geofenceManager.setupGeofencesForShops(shops)
    }
    
    fun removeAllGeofences(context: Context) {
        val geofenceManager = GeofenceManager(context)
        geofenceManager.removeAllGeofences()
    }
    
    fun removeGeofenceForShop(context: Context, shopId: String) {
        val geofenceManager = GeofenceManager(context)
        geofenceManager.removeGeofenceById(shopId)
    }
    
    fun testNotification(context: Context) {
        val notificationManager = xyz.moroku0519.shoppinghelper.notification.ShoppingNotificationManager(context)
        notificationManager.showTestNotification()
    }
}

@Composable
fun AutoGeofenceSetup(shops: List<ShopUi>) {
    val context = LocalContext.current
    
    LaunchedEffect(shops) {
        if (shops.isNotEmpty()) {
            GeofenceIntegration.setupGeofences(context, shops)
        }
    }
}