package xyz.moroku0519.shoppinghelper.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        val TOKYO_STATION = Location(35.6812, 139.7671)
    }
}