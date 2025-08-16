package xyz.moroku0519.shoppinghelper.model

import androidx.compose.ui.graphics.Color

enum class ShopCategory(val displayName: String) {
    GROCERY("スーパー"),
    SUPERMARKET("スーパーマーケット"), // 追加
    PHARMACY("薬局"),
    CONVENIENCE("コンビニ"),
    CONVENIENCE_STORE("コンビニエンスストア"), // 追加
    BAKERY("ベーカリー"),
    DEPARTMENT("デパート"),
    ELECTRONICS("家電"),
    CLOTHING("衣料品"),
    RESTAURANT("レストラン"),
    OTHER("その他");

    val color: Color
        get() = when (this) {
            GROCERY -> Color(0xFF4CAF50)
            SUPERMARKET -> Color(0xFF4CAF50)
            PHARMACY -> Color(0xFF9C27B0)
            CONVENIENCE -> Color(0xFF2196F3)
            CONVENIENCE_STORE -> Color(0xFF2196F3)
            BAKERY -> Color(0xFF795548)
            DEPARTMENT -> Color(0xFFFF5722)
            ELECTRONICS -> Color(0xFF607D8B)
            CLOTHING -> Color(0xFFE91E63)
            RESTAURANT -> Color(0xFFFF9800)
            OTHER -> Color.Gray
        }
}