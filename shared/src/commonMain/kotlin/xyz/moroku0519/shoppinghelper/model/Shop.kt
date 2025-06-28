package xyz.moroku0519.shoppinghelper.model

data class Shop(
    val id: String,
    val name: String,
    val address: String = "",
    val category: ShopCategory = ShopCategory.GROCERY,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)