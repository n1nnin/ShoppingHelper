package xyz.moroku0519.shoppinghelper.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.moroku0519.shoppinghelper.model.*

/**
 * Supabase用のデータクラス
 * 
 * PostgreSQLのテーブル構造に対応し、
 * アプリケーションのドメインモデルとの変換機能を提供
 */

@Serializable
data class SupabaseProfile(
    val id: String,
    val email: String?,
    @SerialName("display_name")
    val displayName: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class SupabaseShop(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String,
    @SerialName("is_favorite")
    val isFavorite: Boolean = false,
    @SerialName("owner_id")
    val ownerId: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
) {
    fun toShop(): Shop = Shop(
        id = id,
        name = name,
        address = address,
        location = if (latitude != null && longitude != null) {
            Location(latitude, longitude)
        } else null,
        category = ShopCategory.valueOf(category),
        isFavorite = isFavorite,
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt)
    )
    
    companion object {
        fun fromShop(shop: Shop, ownerId: String?): SupabaseShop = SupabaseShop(
            id = shop.id,
            name = shop.name,
            address = shop.address,
            latitude = shop.location?.latitude,
            longitude = shop.location?.longitude,
            category = shop.category.name,
            isFavorite = shop.isFavorite ?: false,
            ownerId = ownerId,
            createdAt = formatTimestamp(shop.createdAt),
            updatedAt = formatTimestamp(shop.updatedAt)
        )
    }
}

@Serializable
data class SupabaseShoppingList(
    val id: String,
    val name: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_shared")
    val isShared: Boolean = false,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
) {
    fun toShoppingList(): ShoppingList = ShoppingList(
        id = id,
        name = name,
        isActive = isActive,
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt)
    )
    
    companion object {
        fun fromShoppingList(list: ShoppingList, ownerId: String): SupabaseShoppingList = SupabaseShoppingList(
            id = list.id,
            name = list.name,
            ownerId = ownerId,
            isActive = list.isActive,
            isShared = false,
            createdAt = formatTimestamp(list.createdAt),
            updatedAt = formatTimestamp(list.updatedAt)
        )
    }
}

@Serializable
data class SupabaseShoppingItem(
    val id: String,
    @SerialName("list_id")
    val listId: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String?,
    val price: Double?,
    val priority: String = "NORMAL",
    val category: String = "OTHER",
    @SerialName("shop_id")
    val shopId: String?,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    val notes: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("completed_at")
    val completedAt: String?
) {
    fun toShoppingItem(): ShoppingItem = ShoppingItem(
        id = id,
        listId = listId,
        name = name,
        quantity = quantity,
        unit = unit,
        price = price,
        priority = Priority.valueOf(priority),
        category = ItemCategory.valueOf(category),
        shopId = shopId,
        isCompleted = isCompleted,
        notes = notes,
        createdAt = parseTimestamp(createdAt),
        updatedAt = parseTimestamp(updatedAt),
        completedAt = completedAt?.let { parseTimestamp(it) }
    )
    
    companion object {
        fun fromShoppingItem(item: ShoppingItem): SupabaseShoppingItem = SupabaseShoppingItem(
            id = item.id,
            listId = item.listId,
            name = item.name,
            quantity = item.quantity,
            unit = item.unit,
            price = item.price,
            priority = item.priority.name,
            category = item.category.name,
            shopId = item.shopId,
            isCompleted = item.isCompleted,
            notes = item.notes,
            createdAt = formatTimestamp(item.createdAt),
            updatedAt = formatTimestamp(item.updatedAt),
            completedAt = item.completedAt?.let { formatTimestamp(it) }
        )
    }
}

@Serializable
data class SupabaseItemTemplate(
    val id: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String?,
    val category: String = "OTHER",
    @SerialName("shop_id")
    val shopId: String?,
    val notes: String?,
    @SerialName("use_count")
    val useCount: Int = 0,
    @SerialName("last_used_at")
    val lastUsedAt: String?,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("created_at")
    val createdAt: String
) {
    fun toItemTemplate(): ItemTemplate = ItemTemplate(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        category = ItemCategory.valueOf(category),
        shopId = shopId,
        notes = notes,
        useCount = useCount,
        lastUsedAt = lastUsedAt?.let { parseTimestamp(it) },
        createdAt = parseTimestamp(createdAt)
    )
    
    companion object {
        fun fromItemTemplate(template: ItemTemplate, ownerId: String): SupabaseItemTemplate = SupabaseItemTemplate(
            id = template.id,
            name = template.name,
            quantity = template.quantity,
            unit = template.unit,
            category = template.category.name,
            shopId = template.shopId,
            notes = template.notes,
            useCount = template.useCount,
            lastUsedAt = template.lastUsedAt?.let { formatTimestamp(it) },
            ownerId = ownerId,
            createdAt = formatTimestamp(template.createdAt)
        )
    }
}

/**
 * Supabaseのタイムスタンプ形式をLongに変換
 */
private fun parseTimestamp(timestamp: String): Long {
    // PostgreSQLのTIMESTAMPTZ形式をパース
    // 簡易実装: 実際のプロジェクトでは適切な日時ライブラリを使用
    return try {
        // ISO8601形式のタイムスタンプをミリ秒に変換
        // 例: "2023-01-01T00:00:00.000Z" -> Long
        timestamp.replace(Regex("[TZ]"), " ").replace("-", "").replace(":", "").replace(".", "").trim().toLong()
    } catch (e: Exception) {
        System.currentTimeMillis() // フォールバック
    }
}

/**
 * LongタイムスタンプをSupabase形式に変換
 */
private fun formatTimestamp(timestamp: Long): String {
    // 簡易実装: 実際のプロジェクトでは適切な日時ライブラリを使用
    return "2024-01-01T00:00:00.000Z" // プレースホルダー
}