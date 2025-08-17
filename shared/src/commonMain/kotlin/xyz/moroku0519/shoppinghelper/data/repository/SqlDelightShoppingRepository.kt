package xyz.moroku0519.shoppinghelper.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase
import xyz.moroku0519.shoppinghelper.model.*
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis
import xyz.moroku0519.shoppinghelper.util.generateId

class SqlDelightShoppingRepository(
    private val database: ShoppingDatabase
) : ShoppingRepository {
    
    // Shopping Lists
    override fun getAllLists(): Flow<List<ShoppingList>> {
        return database.shoppingListQueries.getAllLists()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbLists ->
                dbLists.map { dbList ->
                    ShoppingList(
                        id = dbList.id,
                        name = dbList.name,
                        isActive = dbList.is_active == 1L,
                        createdAt = dbList.created_at,
                        updatedAt = dbList.updated_at
                    )
                }
            }
    }
    
    override fun getActiveList(): Flow<ShoppingList?> {
        return database.shoppingListQueries.getActiveList()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { dbList ->
                dbList?.let {
                    ShoppingList(
                        id = it.id,
                        name = it.name,
                        isActive = it.is_active == 1L,
                        createdAt = it.created_at,
                        updatedAt = it.updated_at
                    )
                }
            }
    }
    
    override suspend fun createList(name: String): ShoppingList {
        val newList = ShoppingList(
            id = generateId(),
            name = name,
            isActive = true,
            createdAt = currentTimeMillis(),
            updatedAt = currentTimeMillis()
        )
        
        database.transaction {
            // Set all other lists as inactive
            database.shoppingListQueries.setActiveList(newList.id)
            
            // Insert the new list
            database.shoppingListQueries.insertList(
                id = newList.id,
                name = newList.name,
                is_active = if (newList.isActive) 1L else 0L,
                created_at = newList.createdAt,
                updated_at = newList.updatedAt
            )
        }
        
        return newList
    }
    
    override suspend fun updateList(list: ShoppingList) {
        val updatedAt = currentTimeMillis()
        database.shoppingListQueries.updateList(
            name = list.name,
            is_active = if (list.isActive) 1L else 0L,
            updated_at = updatedAt,
            id = list.id
        )
    }
    
    override suspend fun deleteList(listId: String) {
        database.transaction {
            // Items will be deleted automatically due to CASCADE
            database.shoppingListQueries.deleteList(listId)
        }
    }
    
    // Shopping Items
    override fun getItemsByListId(listId: String): Flow<List<ShoppingItem>> {
        return database.shoppingItemQueries.getItemsByListId(listId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbItems ->
                dbItems.map { dbItem ->
                    ShoppingItem(
                        id = dbItem.id,
                        listId = dbItem.list_id,
                        name = dbItem.name,
                        quantity = dbItem.quantity.toInt(),
                        unit = dbItem.unit,
                        price = dbItem.price,
                        priority = dbItem.priority,
                        category = dbItem.category,
                        shopId = dbItem.shop_id,
                        isCompleted = dbItem.is_completed == 1L,
                        notes = dbItem.notes,
                        createdAt = dbItem.created_at,
                        updatedAt = dbItem.updated_at,
                        completedAt = dbItem.completed_at
                    )
                }
            }
    }
    
    override fun getIncompleteItemsByShop(shopId: String): Flow<List<ShoppingItem>> {
        return database.shoppingItemQueries.getIncompleteItemsByShop(shopId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbItems ->
                dbItems.map { dbItem ->
                    ShoppingItem(
                        id = dbItem.id,
                        listId = dbItem.list_id,
                        name = dbItem.name,
                        quantity = dbItem.quantity.toInt(),
                        unit = dbItem.unit,
                        price = dbItem.price,
                        priority = dbItem.priority,
                        category = dbItem.category,
                        shopId = dbItem.shop_id,
                        isCompleted = dbItem.is_completed == 1L,
                        notes = dbItem.notes,
                        createdAt = dbItem.created_at,
                        updatedAt = dbItem.updated_at,
                        completedAt = dbItem.completed_at
                    )
                }
            }
    }
    
    override suspend fun addItem(item: ShoppingItem) {
        database.shoppingItemQueries.insertItem(
            id = item.id,
            list_id = item.listId,
            name = item.name,
            quantity = item.quantity.toLong(),
            unit = item.unit,
            price = item.price,
            priority = item.priority,
            category = item.category,
            shop_id = item.shopId,
            is_completed = if (item.isCompleted) 1L else 0L,
            notes = item.notes,
            created_at = item.createdAt,
            updated_at = item.updatedAt,
            completed_at = item.completedAt
        )
    }
    
    override suspend fun updateItem(item: ShoppingItem) {
        database.shoppingItemQueries.updateItem(
            name = item.name,
            quantity = item.quantity.toLong(),
            unit = item.unit,
            price = item.price,
            priority = item.priority,
            category = item.category,
            shop_id = item.shopId,
            is_completed = if (item.isCompleted) 1L else 0L,
            notes = item.notes,
            updated_at = currentTimeMillis(),
            completed_at = item.completedAt,
            id = item.id
        )
    }
    
    override suspend fun toggleItemComplete(itemId: String) {
        val currentTime = currentTimeMillis()
        // Get current item state
        val currentItem = database.shoppingItemQueries.getItemById(itemId).executeAsOne()
        val newCompletedState = currentItem.is_completed == 0L
        val newCompletedAt = if (newCompletedState) currentTime else null
        
        database.shoppingItemQueries.updateItemCompleted(
            is_completed = if (newCompletedState) 1L else 0L,
            completed_at = newCompletedAt,
            updated_at = currentTime,
            id = itemId
        )
    }
    
    override suspend fun deleteItem(itemId: String) {
        database.shoppingItemQueries.deleteItem(itemId)
    }
    
    override suspend fun deleteCompletedItems(listId: String) {
        database.shoppingItemQueries.deleteCompletedItems(listId)
    }
    
    // Shops
    override fun getAllShops(): Flow<List<Shop>> {
        return database.shopQueries.getAllShops()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbShops ->
                dbShops.map { dbShop ->
                    Shop(
                        id = dbShop.id,
                        name = dbShop.name,
                        address = dbShop.address,
                        location = if (dbShop.latitude != null && dbShop.longitude != null) {
                            Location(
                                latitude = dbShop.latitude,
                                longitude = dbShop.longitude
                            )
                        } else null,
                        category = dbShop.category,
                        isFavorite = dbShop.is_favorite == 1L,
                        createdAt = dbShop.created_at,
                        updatedAt = dbShop.updated_at
                    )
                }
            }
    }
    
    override fun getFavoriteShops(): Flow<List<Shop>> {
        return database.shopQueries.getFavoriteShops()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbShops ->
                dbShops.map { dbShop ->
                    Shop(
                        id = dbShop.id,
                        name = dbShop.name,
                        address = dbShop.address,
                        location = if (dbShop.latitude != null && dbShop.longitude != null) {
                            Location(
                                latitude = dbShop.latitude,
                                longitude = dbShop.longitude
                            )
                        } else null,
                        category = dbShop.category,
                        isFavorite = dbShop.is_favorite == 1L,
                        createdAt = dbShop.created_at,
                        updatedAt = dbShop.updated_at
                    )
                }
            }
    }
    
    override suspend fun addShop(shop: Shop) {
        database.shopQueries.insertShop(
            id = shop.id,
            name = shop.name,
            address = shop.address,
            latitude = shop.location?.latitude,
            longitude = shop.location?.longitude,
            category = shop.category,
            is_favorite = if (shop.isFavorite == true) 1L else 0L,
            created_at = shop.createdAt,
            updated_at = shop.updatedAt
        )
    }
    
    override suspend fun updateShop(shop: Shop) {
        database.shopQueries.updateShop(
            name = shop.name,
            address = shop.address,
            latitude = shop.location?.latitude,
            longitude = shop.location?.longitude,
            category = shop.category,
            is_favorite = if (shop.isFavorite == true) 1L else 0L,
            updated_at = currentTimeMillis(),
            id = shop.id
        )
    }
    
    override suspend fun deleteShop(shopId: String) {
        database.shopQueries.deleteShop(shopId)
    }
    
    // Templates
    override fun getAllTemplates(): Flow<List<ItemTemplate>> {
        return database.itemTemplateQueries.getAllTemplates()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbTemplates ->
                dbTemplates.map { dbTemplate ->
                    ItemTemplate(
                        id = dbTemplate.id,
                        name = dbTemplate.name,
                        quantity = dbTemplate.quantity.toInt(),
                        unit = dbTemplate.unit,
                        category = dbTemplate.category,
                        shopId = dbTemplate.shop_id,
                        notes = dbTemplate.notes,
                        useCount = dbTemplate.use_count.toInt(),
                        lastUsedAt = dbTemplate.last_used_at,
                        createdAt = dbTemplate.created_at
                    )
                }
            }
    }
    
    override fun getTemplatesByCategory(category: ItemCategory): Flow<List<ItemTemplate>> {
        return database.itemTemplateQueries.getTemplatesByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { dbTemplates ->
                dbTemplates.map { dbTemplate ->
                    ItemTemplate(
                        id = dbTemplate.id,
                        name = dbTemplate.name,
                        quantity = dbTemplate.quantity.toInt(),
                        unit = dbTemplate.unit,
                        category = dbTemplate.category,
                        shopId = dbTemplate.shop_id,
                        notes = dbTemplate.notes,
                        useCount = dbTemplate.use_count.toInt(),
                        lastUsedAt = dbTemplate.last_used_at,
                        createdAt = dbTemplate.created_at
                    )
                }
            }
    }
    
    override suspend fun addTemplate(template: ItemTemplate) {
        database.itemTemplateQueries.insertTemplate(
            id = template.id,
            name = template.name,
            quantity = template.quantity.toLong(),
            unit = template.unit,
            category = template.category,
            shop_id = template.shopId,
            notes = template.notes,
            use_count = template.useCount.toLong(),
            last_used_at = template.lastUsedAt,
            created_at = template.createdAt
        )
    }
    
    override suspend fun updateTemplateUsage(templateId: String) {
        val currentTime = currentTimeMillis()
        database.itemTemplateQueries.incrementUseCount(
            last_used_at = currentTime,
            id = templateId
        )
    }
    
    override suspend fun deleteTemplate(templateId: String) {
        database.itemTemplateQueries.deleteTemplate(templateId)
    }
}