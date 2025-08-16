package xyz.moroku0519.shoppinghelper.model

import kotlinx.serialization.Serializable
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

@Serializable
data class Shop(
    val id: String,
    val name: String,
    val address: String? = null,
    val location: Location? = null,
    val category: ShopCategory = ShopCategory.GROCERY,
    val phoneNumber: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean? = false,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis()
) {
    val latitude: Double? get() = location?.latitude
    val longitude: Double? get() = location?.longitude
}