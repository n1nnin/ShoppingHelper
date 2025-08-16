package xyz.moroku0519.shoppinghelper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import xyz.moroku0519.shoppinghelper.model.*
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis
import xyz.moroku0519.shoppinghelper.util.generateId

class InMemoryShoppingRepository : ShoppingRepository {
    
    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    private val _templates = MutableStateFlow<List<ItemTemplate>>(emptyList())
    
    init {
        // Create default list if none exists
        val defaultList = ShoppingList(
            id = generateId(),
            name = "買い物リスト",
            isActive = true,
            createdAt = currentTimeMillis(),
            updatedAt = currentTimeMillis()
        )
        _lists.value = listOf(defaultList)
        
        // Add sample shops
        val sampleShops = listOf(
            Shop(
                id = "shop1",
                name = "イオン",
                address = "東京都新宿区",
                location = Location(35.6895, 139.6917),
                category = ShopCategory.SUPERMARKET,
                createdAt = currentTimeMillis(),
                updatedAt = currentTimeMillis()
            ),
            Shop(
                id = "shop2",
                name = "ツルハドラッグ",
                address = "東京都渋谷区",
                location = Location(35.6584, 139.7016),
                category = ShopCategory.PHARMACY,
                createdAt = currentTimeMillis(),
                updatedAt = currentTimeMillis()
            ),
            Shop(
                id = "shop3",
                name = "セブンイレブン",
                address = "東京都品川区",
                location = Location(35.6284, 139.7387),
                category = ShopCategory.CONVENIENCE_STORE,
                createdAt = currentTimeMillis(),
                updatedAt = currentTimeMillis()
            )
        )
        _shops.value = sampleShops
    }
    
    // Shopping Lists
    override fun getAllLists(): Flow<List<ShoppingList>> = _lists
    
    override fun getActiveList(): Flow<ShoppingList?> = _lists.map { lists ->
        lists.firstOrNull { it.isActive }
    }
    
    override suspend fun createList(name: String): ShoppingList {
        val newList = ShoppingList(
            id = generateId(),
            name = name,
            isActive = false, // Don't automatically activate
            createdAt = currentTimeMillis(),
            updatedAt = currentTimeMillis()
        )
        _lists.value = _lists.value + newList
        return newList
    }
    
    override suspend fun updateList(list: ShoppingList) {
        _lists.value = _lists.value.map { 
            if (it.id == list.id) list.copy(updatedAt = currentTimeMillis()) else it 
        }
    }
    
    override suspend fun deleteList(listId: String) {
        _lists.value = _lists.value.filter { it.id != listId }
        // Also delete associated items
        _items.value = _items.value.filter { it.listId != listId }
    }
    
    // Shopping Items
    override fun getItemsByListId(listId: String): Flow<List<ShoppingItem>> = _items.map { items ->
        items.filter { it.listId == listId }
            .sortedWith(
                compareBy<ShoppingItem> { it.isCompleted }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.createdAt }
            )
    }
    
    override fun getIncompleteItemsByShop(shopId: String): Flow<List<ShoppingItem>> = _items.map { items ->
        items.filter { !it.isCompleted && it.shopId == shopId }
            .sortedWith(
                compareByDescending<ShoppingItem> { it.priority.ordinal }
                    .thenBy { it.createdAt }
            )
    }
    
    override suspend fun addItem(item: ShoppingItem) {
        _items.value = _items.value + item
    }
    
    override suspend fun updateItem(item: ShoppingItem) {
        _items.value = _items.value.map { 
            if (it.id == item.id) item.copy(updatedAt = currentTimeMillis()) else it 
        }
    }
    
    override suspend fun toggleItemComplete(itemId: String) {
        val currentTime = currentTimeMillis()
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    isCompleted = !item.isCompleted,
                    completedAt = if (!item.isCompleted) currentTime else null,
                    updatedAt = currentTime
                )
            } else item
        }
    }
    
    override suspend fun deleteItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
    }
    
    override suspend fun deleteCompletedItems(listId: String) {
        _items.value = _items.value.filter { !(it.listId == listId && it.isCompleted) }
    }
    
    // Shops
    override fun getAllShops(): Flow<List<Shop>> = _shops
    
    override fun getFavoriteShops(): Flow<List<Shop>> = _shops.map { shops ->
        shops.filter { it.isFavorite == true }
    }
    
    override suspend fun addShop(shop: Shop) {
        _shops.value = _shops.value + shop
    }
    
    override suspend fun updateShop(shop: Shop) {
        _shops.value = _shops.value.map { 
            if (it.id == shop.id) shop.copy(updatedAt = currentTimeMillis()) else it 
        }
    }
    
    override suspend fun deleteShop(shopId: String) {
        _shops.value = _shops.value.filter { it.id != shopId }
        // Remove shop reference from items
        _items.value = _items.value.map { item ->
            if (item.shopId == shopId) item.copy(shopId = null, updatedAt = currentTimeMillis())
            else item
        }
    }
    
    // Templates
    override fun getAllTemplates(): Flow<List<ItemTemplate>> = _templates
    
    override fun getTemplatesByCategory(category: ItemCategory): Flow<List<ItemTemplate>> = 
        _templates.map { templates ->
            templates.filter { it.category == category }
                .sortedByDescending { it.useCount }
        }
    
    override suspend fun addTemplate(template: ItemTemplate) {
        _templates.value = _templates.value + template
    }
    
    override suspend fun updateTemplateUsage(templateId: String) {
        val currentTime = currentTimeMillis()
        _templates.value = _templates.value.map { template ->
            if (template.id == templateId) {
                template.copy(
                    useCount = template.useCount + 1,
                    lastUsedAt = currentTime
                )
            } else template
        }
    }
    
    override suspend fun deleteTemplate(templateId: String) {
        _templates.value = _templates.value.filter { it.id != templateId }
    }
}