package xyz.moroku0519.shoppinghelper.data.repository

import kotlinx.coroutines.flow.Flow
import xyz.moroku0519.shoppinghelper.model.*

interface ShoppingRepository {
    // Shopping Lists
    fun getAllLists(): Flow<List<ShoppingList>>
    fun getActiveList(): Flow<ShoppingList?>
    suspend fun createList(name: String): ShoppingList
    suspend fun updateList(list: ShoppingList)
    suspend fun deleteList(listId: String)
    
    // Shopping Items
    fun getItemsByListId(listId: String): Flow<List<ShoppingItem>>
    fun getIncompleteItemsByShop(shopId: String): Flow<List<ShoppingItem>>
    suspend fun addItem(item: ShoppingItem)
    suspend fun updateItem(item: ShoppingItem)
    suspend fun toggleItemComplete(itemId: String)
    suspend fun deleteItem(itemId: String)
    suspend fun deleteCompletedItems(listId: String)
    
    // Shops
    fun getAllShops(): Flow<List<Shop>>
    fun getFavoriteShops(): Flow<List<Shop>>
    suspend fun addShop(shop: Shop)
    suspend fun updateShop(shop: Shop)
    suspend fun deleteShop(shopId: String)
    
    // Templates
    fun getAllTemplates(): Flow<List<ItemTemplate>>
    fun getTemplatesByCategory(category: ItemCategory): Flow<List<ItemTemplate>>
    suspend fun addTemplate(template: ItemTemplate)
    suspend fun updateTemplateUsage(templateId: String)
    suspend fun deleteTemplate(templateId: String)
}