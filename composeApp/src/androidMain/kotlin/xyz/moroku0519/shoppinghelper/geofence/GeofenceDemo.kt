package xyz.moroku0519.shoppinghelper.geofence

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.notification.ShoppingNotificationManager
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel

@Composable
fun GeofenceTestPanel() {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Geofence機能テスト",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = "以下のボタンでGeofence機能をテストできます",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = { setupTestGeofences(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("テスト用Geofenceを設定")
            }
            
            Button(
                onClick = { testNotification(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("通知をテスト")
            }
            
            Button(
                onClick = { removeAllGeofences(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("全てのGeofenceを削除")
            }
            
            Text(
                text = "注意: 位置情報権限とバックグラウンド位置情報権限、通知権限が必要です",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun setupTestGeofences(context: Context) {
    val testShops = listOf(
        Shop(
            id = "test_shop_1",
            name = "テスト店舗1",
            address = "テスト住所1",
            category = ShopCategory.GROCERY,
            latitude = 35.6762, // 東京駅周辺
            longitude = 139.6503
        ).toUiModel(pendingItemsCount = 2, totalItemsCount = 5),
        
        Shop(
            id = "test_shop_2", 
            name = "テスト店舗2",
            address = "テスト住所2",
            category = ShopCategory.PHARMACY,
            latitude = 35.6812, // 東京駅周辺
            longitude = 139.7671
        ).toUiModel(pendingItemsCount = 1, totalItemsCount = 3)
    )
    
    val geofenceManager = GeofenceManager(context)
    geofenceManager.setupGeofencesForShops(testShops)
}

private fun testNotification(context: Context) {
    val notificationManager = ShoppingNotificationManager(context)
    notificationManager.showTestNotification()
}

private fun removeAllGeofences(context: Context) {
    val geofenceManager = GeofenceManager(context)
    geofenceManager.removeAllGeofences()
}