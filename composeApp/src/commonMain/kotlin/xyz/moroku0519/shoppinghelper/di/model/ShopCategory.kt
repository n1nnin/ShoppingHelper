package xyz.moroku0519.shoppinghelper.di.model

import androidx.compose.ui.graphics.Color

enum class ShopCategory {
    GROCERY,
    PHARMACY,
    CONVENIENCE,
    DEPARTMENT,
    ELECTRONICS,
    CLOTHING,
    RESTAURANT,
    OTHER;

    val color: Color
        get() = when (this) {
            GROCERY -> Color(0xFF4CAF50)
            PHARMACY -> Color(0xFF9C27B0)
            CONVENIENCE -> Color(0xFF2196F3)
            DEPARTMENT -> Color(0xFFFF5722)
            ELECTRONICS -> Color(0xFF607D8B)
            CLOTHING -> Color(0xFFE91E63)
            RESTAURANT -> Color(0xFFFF9800)
            OTHER -> Color.Gray
        }
}