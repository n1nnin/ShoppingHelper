package xyz.moroku0519.shoppinghelper.model

import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

data class Shop(
    val id: String,
    val name: String,
    val address: String? = null,
    val category: ShopCategory = ShopCategory.GROCERY,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = currentTimeMillis()
)