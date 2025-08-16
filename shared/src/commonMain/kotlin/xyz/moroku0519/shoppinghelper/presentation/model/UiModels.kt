package xyz.moroku0519.shoppinghelper.presentation.model

import xyz.moroku0519.shoppinghelper.model.*

// Temporary simple UI models for shared module
data class ShoppingItemUi(
    val id: String,
    val name: String,
    val isCompleted: Boolean,
    val shopName: String?,
    val shopId: String?,
    val priority: Priority,
    val category: ItemCategory
)

data class ShopUi(
    val id: String,
    val name: String,
    val address: String,
    val category: ShopCategory,
    val pendingItemsCount: Int = 0,
    val totalItemsCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    companion object {
        fun fromShop(shop: Shop, pendingItemsCount: Int = 0, totalItemsCount: Int = 0): ShopUi {
            return ShopUi(
                id = shop.id,
                name = shop.name,
                address = shop.address ?: "",
                category = shop.category,
                pendingItemsCount = pendingItemsCount,
                totalItemsCount = totalItemsCount,
                latitude = shop.latitude,
                longitude = shop.longitude
            )
        }
    }
}