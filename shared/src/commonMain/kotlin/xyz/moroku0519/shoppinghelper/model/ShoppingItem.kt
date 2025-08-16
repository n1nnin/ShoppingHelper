package xyz.moroku0519.shoppinghelper.model

import kotlinx.serialization.Serializable
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

@Serializable
data class ShoppingItem(
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String? = null,
    val price: Double? = null,
    val priority: Priority = Priority.NORMAL,
    val category: ItemCategory = ItemCategory.OTHER,
    val shopId: String? = null,
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis(),
    val completedAt: Long? = null
)