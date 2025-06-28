package xyz.moroku0519.shoppinghelper.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.notification.ShoppingNotificationManager
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Geofence event received")
        
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            Log.e(TAG, "Geofenceエラー: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        Log.d(TAG, "Geofence transition: $geofenceTransition")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                Log.d(TAG, "Triggered geofences count: ${triggeringGeofences?.size}")

                triggeringGeofences?.forEach { geofence ->
                    handleGeofenceEnter(context, geofence.requestId)
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "User exited geofence area")
            }
            else -> {
                Log.w(TAG, "Unknown geofence transition: $geofenceTransition")
            }
        }
    }

    private fun handleGeofenceEnter(context: Context, shopId: String) {
        Log.d(TAG, "お店に近づきました: $shopId")

        try {
            val shop = getShopData(shopId)
            
            if (shop.pendingItemsCount > 0) {
                val notificationManager = ShoppingNotificationManager(context)
                notificationManager.showShoppingReminder(shop)
                Log.d(TAG, "通知を送信しました: ${shop.name}")
            } else {
                Log.d(TAG, "このお店に買い物リストがないため通知をスキップしました: ${shop.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geofence処理中にエラーが発生しました", e)
        }
    }

    private fun getShopData(shopId: String): ShopUi {
        return createSampleShop(shopId)
    }

    // サンプル店舗データ生成（実際はDBから取得）
    private fun createSampleShop(shopId: String): ShopUi {
        return when (shopId) {
            "shop1" -> ShopUi(
                id = shopId,
                name = "イオン渋谷店",
                address = "東京都渋谷区神南1-1-1",
                category = ShopCategory.GROCERY,
                pendingItemsCount = 3,
                totalItemsCount = 8
            )
            "shop2" -> ShopUi(
                id = shopId,
                name = "ツルハドラッグ新宿店",
                address = "東京都新宿区新宿3-1-1",
                category = ShopCategory.PHARMACY,
                pendingItemsCount = 1,
                totalItemsCount = 2
            )
            else -> ShopUi(
                id = shopId,
                name = "近くのお店",
                address = "住所不明",
                category = ShopCategory.OTHER,
                pendingItemsCount = 1,
                totalItemsCount = 1
            )
        }
    }
}