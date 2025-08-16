package xyz.moroku0519.shoppinghelper.model

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val id: String,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ItemTemplate(
    val id: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String? = null,
    val category: ItemCategory = ItemCategory.OTHER,
    val shopId: String? = null,
    val notes: String? = null,
    val useCount: Int = 0,
    val lastUsedAt: Long? = null,
    val createdAt: Long
)