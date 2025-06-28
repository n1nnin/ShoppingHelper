package xyz.moroku0519.shoppinghelper.domain.model

import androidx.compose.ui.graphics.Color


enum class Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    val color: Color
        get() = when (this) {
            LOW -> Color.Gray
            NORMAL -> Color.Blue
            HIGH -> Color(0xFFFF9800) // Orange
            URGENT -> Color.Red
        }
}