package xyz.moroku0519.shoppinghelper.model

data class ShoppingItem(
    val id: String,
    val name: String,
    val isCompleted: Boolean = false,
    val shopId: String? = null,
    val priority: Priority = Priority.NORMAL,
    val createdAt: Long = System.currentTimeMillis()
)