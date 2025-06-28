package xyz.moroku0519.shoppinghelper.model

data class Geofence(
    val id: String,
    val shopId: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f, // meters
    val isActive: Boolean = true
)
