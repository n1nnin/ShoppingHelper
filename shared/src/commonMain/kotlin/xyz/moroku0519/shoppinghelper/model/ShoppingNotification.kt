package xyz.moroku0519.shoppinghelper.model

data class ShoppingNotification(
    val id: String,
    val shopId: String,
    val shopName: String,
    val pendingItemsCount: Int,
    val message: String,
    val timestamp: Long
)