package xyz.moroku0519.shoppinghelper.model

import androidx.compose.ui.graphics.Color

enum class ItemCategory(val displayName: String) {
    FOOD("食品"),
    DAILY_GOODS("日用品"),
    MEDICINE("薬品"),
    CLOTHING("衣料品"),
    ELECTRONICS("家電・電子機器"),
    BOOKS_STATIONERY("書籍・文房具"),
    COSMETICS("美容・コスメ"),
    SPORTS("スポーツ用品"),
    HOBBY("趣味・娯楽"),
    OTHER("その他");

    val color: Color
        get() = when (this) {
            FOOD -> Color(0xFF4CAF50)           // Green
            DAILY_GOODS -> Color(0xFF2196F3)    // Blue
            MEDICINE -> Color(0xFFF44336)       // Red
            CLOTHING -> Color(0xFF9C27B0)       // Purple
            ELECTRONICS -> Color(0xFF607D8B)    // Blue Grey
            BOOKS_STATIONERY -> Color(0xFFFF9800) // Orange
            COSMETICS -> Color(0xFFE91E63)      // Pink
            SPORTS -> Color(0xFF795548)         // Brown
            HOBBY -> Color(0xFF673AB7)          // Deep Purple
            OTHER -> Color(0xFF9E9E9E)          // Grey
        }
}