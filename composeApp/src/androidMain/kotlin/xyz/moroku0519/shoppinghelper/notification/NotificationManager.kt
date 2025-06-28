package xyz.moroku0519.shoppinghelper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xyz.moroku0519.shoppinghelper.MainActivity
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

class ShoppingNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "shopping_reminders"
        private const val CHANNEL_NAME = "買い物リマインダー"
        private const val CHANNEL_DESCRIPTION = "お店に近づいたときの買い物リマインダー通知"
    }

    init {
        createNotificationChannel()
    }

    // 通知チャンネル作成
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // 買い物リマインダー通知を表示
    fun showShoppingReminder(shop: ShopUi) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("shop_id", shop.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            shop.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = when {
            shop.pendingItemsCount == 1 -> "1件の買い物があります"
            shop.pendingItemsCount > 1 -> "${shop.pendingItemsCount}件の買い物があります"
            else -> "買い物リストを確認してください"
        }

        val categoryIcon = when (shop.category) {
            ShopCategory.GROCERY -> android.R.drawable.ic_menu_gallery
            ShopCategory.PHARMACY -> android.R.drawable.ic_menu_help
            ShopCategory.CONVENIENCE -> android.R.drawable.ic_menu_today
            ShopCategory.DEPARTMENT -> android.R.drawable.ic_menu_preferences
            ShopCategory.ELECTRONICS -> android.R.drawable.ic_menu_manage
            else -> android.R.drawable.ic_menu_add
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(categoryIcon)
            .setContentTitle("${shop.name}の近くにいます")
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${shop.name}の近くを通りかかりました。\n$notificationText\n\nタップして買い物リストを確認")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(0xFF00FF00.toInt(), 1000, 500)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(shop.id.hashCode(), notification)
            println("通知を表示しました: ${shop.name}")
        } catch (securityException: SecurityException) {
            println("通知権限が不足しています")
        }
    }

    // テスト用の通知表示
    fun showTestNotification() {
        val testShop = ShopUi(
            id = "test",
            name = "テスト店舗",
            address = "テスト住所",
            category = ShopCategory.GROCERY,
            pendingItemsCount = 3,
            totalItemsCount = 5
        )
        showShoppingReminder(testShop)
    }
}