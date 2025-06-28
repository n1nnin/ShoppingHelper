package xyz.moroku0519.shoppinghelper.model

data class Location(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        val TOKYO_STATION = Location(35.6812, 139.7671)
    }
}