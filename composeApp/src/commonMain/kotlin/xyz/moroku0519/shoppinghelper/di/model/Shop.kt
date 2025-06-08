package xyz.moroku0519.shoppinghelper.di.model

data class Shop(
    val id: String,
    val name: String,
    val address: String = "",
    val category: ShopCategory = ShopCategory.GROCERY,
    val createdAt: Long = System.currentTimeMillis()
)