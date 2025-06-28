package xyz.moroku0519.shoppinghelper.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    fun setupGeofencesForShops(shops: List<ShopUi>) {
        // 権限チェック
        if (!hasLocationPermission()) {
            println("位置情報権限が不足しています")
            return
        }

        val geofenceList = shops.mapNotNull { shop ->
            if (shop.latitude != null && shop.longitude != null && shop.pendingItemsCount > 0) {
                Geofence.Builder()
                    .setRequestId(shop.id)
                    .setCircularRegion(
                        shop.latitude,
                        shop.longitude,
                        100f // 100メートル
                    )
                    .setExpirationDuration(24 * 60 * 60 * 1000L) // 24時間
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            } else {
                null
            }
        }

        if (geofenceList.isNotEmpty()) {
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()

            try {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener {
                        println("Geofence設定成功: ${geofenceList.size}件のお店")
                    }
                    .addOnFailureListener { e ->
                        println("Geofence設定失敗: ${e.message}")
                    }
            } catch (securityException: SecurityException) {
                println("Geofence設定権限エラー: ${securityException.message}")
            }
        } else {
            println("設定対象のお店がありません")
        }
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                println("全てのGeofenceを削除しました")
            }
            .addOnFailureListener { e ->
                println("Geofence削除失敗: ${e.message}")
            }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        return fineLocation && backgroundLocation
    }
    
    fun removeGeofenceById(shopId: String) {
        geofencingClient.removeGeofences(listOf(shopId))
            .addOnSuccessListener {
                println("Geofence削除成功: $shopId")
            }
            .addOnFailureListener { e ->
                println("Geofence削除失敗: ${e.message}")
            }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}