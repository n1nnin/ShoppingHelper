package xyz.moroku0519.shoppinghelper.model

import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

data class ShoppingItem(
    val id: String,
    val name: String,
    val isCompleted: Boolean = false,
    val shopId: String? = null,
    val priority: Priority = Priority.NORMAL,
    val category: ItemCategory = ItemCategory.OTHER,
    val createdAt: Long = currentTimeMillis()
)