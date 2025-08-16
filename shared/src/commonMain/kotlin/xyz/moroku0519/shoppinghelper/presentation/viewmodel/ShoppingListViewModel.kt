package xyz.moroku0519.shoppinghelper.presentation.viewmodel

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.moroku0519.shoppinghelper.data.repository.ShoppingRepository
import xyz.moroku0519.shoppinghelper.model.*
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis
import xyz.moroku0519.shoppinghelper.util.generateId

expect open class ViewModel() {
    protected val viewModelScope: kotlinx.coroutines.CoroutineScope
}

class ShoppingListViewModel(
    private val repository: ShoppingRepository
) : ViewModel() {
    
    private val _selectedListId = MutableStateFlow<String?>(null)
    
    val activeLists = repository.getAllLists()
        .map { lists -> lists.filter { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val allLists = repository.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val shops = repository.getAllShops()
        .map { shopList -> shopList.map { shop -> ShopUi.fromShop(shop) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val currentListItems = _selectedListId
        .flatMapLatest { listId ->
            if (listId != null) {
                repository.getItemsByListId(listId)
            } else {
                // Use active list if no specific list selected
                repository.getActiveList().flatMapLatest { activeList ->
                    if (activeList != null) {
                        repository.getItemsByListId(activeList.id)
                    } else {
                        flowOf(emptyList())
                    }
                }
            }
        }
        .map { items ->
            items.map { item ->
                val shop = shops.value.firstOrNull { it.id == item.shopId }
                ShoppingItemUi(
                    id = item.id,
                    name = item.name,
                    isCompleted = item.isCompleted,
                    shopName = shop?.name,
                    shopId = item.shopId,
                    priority = item.priority,
                    category = item.category
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val templates = repository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    fun selectList(listId: String?) {
        _selectedListId.value = listId
    }
    
    fun addItem(
        name: String,
        shopId: String?,
        priority: Priority,
        category: ItemCategory,
        quantity: Int = 1,
        unit: String? = null,
        price: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val activeList = repository.getActiveList().first()
            if (activeList != null) {
                val newItem = ShoppingItem(
                    id = generateId(),
                    listId = activeList.id,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    priority = priority,
                    category = category,
                    shopId = shopId,
                    notes = notes,
                    createdAt = currentTimeMillis(),
                    updatedAt = currentTimeMillis()
                )
                repository.addItem(newItem)
            }
        }
    }
    
    fun updateItem(
        itemId: String,
        name: String,
        shopId: String?,
        priority: Priority,
        category: ItemCategory,
        quantity: Int = 1,
        unit: String? = null,
        price: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val currentItems = currentListItems.value
            val existingItem = currentItems.firstOrNull { it.id == itemId }
            if (existingItem != null) {
                // We need to get the full ShoppingItem, not just the UI model
                val updatedItem = ShoppingItem(
                    id = itemId,
                    listId = _selectedListId.value ?: "", // This will need to be properly handled
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    price = price,
                    priority = priority,
                    category = category,
                    shopId = shopId,
                    isCompleted = existingItem.isCompleted,
                    notes = notes,
                    createdAt = currentTimeMillis(), // Keep original if available
                    updatedAt = currentTimeMillis()
                )
                repository.updateItem(updatedItem)
            }
        }
    }
    
    fun toggleItemComplete(itemId: String) {
        viewModelScope.launch {
            repository.toggleItemComplete(itemId)
        }
    }
    
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }
    
    fun deleteCompletedItems() {
        viewModelScope.launch {
            val activeList = repository.getActiveList().first()
            if (activeList != null) {
                repository.deleteCompletedItems(activeList.id)
            }
        }
    }
    
    fun createList(name: String) {
        viewModelScope.launch {
            repository.createList(name)
        }
    }
    
    fun addShop(
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        category: ShopCategory,
        phoneNumber: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val newShop = Shop(
                id = generateId(),
                name = name,
                address = address,
                location = if (latitude != null && longitude != null) {
                    Location(latitude, longitude)
                } else null,
                category = category,
                phoneNumber = phoneNumber,
                notes = notes,
                createdAt = currentTimeMillis(),
                updatedAt = currentTimeMillis()
            )
            repository.addShop(newShop)
        }
    }
    
    fun updateShop(
        shopId: String,
        name: String,
        address: String?,
        latitude: Double?,
        longitude: Double?,
        category: ShopCategory,
        phoneNumber: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            // Get the actual Shop data from repository instead of ShopUi
            val allShops = repository.getAllShops().first()
            val existingShop = allShops.firstOrNull { it.id == shopId }
            
            if (existingShop != null) {
                val updatedShop = Shop(
                    id = shopId,
                    name = name,
                    address = address,
                    location = if (latitude != null && longitude != null) {
                        Location(latitude, longitude)
                    } else null,
                    category = category,
                    phoneNumber = phoneNumber,
                    notes = notes,
                    isFavorite = existingShop.isFavorite,
                    createdAt = existingShop.createdAt,
                    updatedAt = currentTimeMillis()
                )
                repository.updateShop(updatedShop)
            }
        }
    }
    
    fun deleteShop(shopId: String) {
        viewModelScope.launch {
            repository.deleteShop(shopId)
        }
    }
    
    fun addTemplate(item: ShoppingItem) {
        viewModelScope.launch {
            val template = ItemTemplate(
                id = generateId(),
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                category = item.category,
                shopId = item.shopId,
                notes = item.notes,
                createdAt = currentTimeMillis()
            )
            repository.addTemplate(template)
        }
    }
    
    fun addItemFromTemplate(template: ItemTemplate) {
        addItem(
            name = template.name,
            shopId = template.shopId,
            priority = Priority.NORMAL,
            category = template.category,
            quantity = template.quantity,
            unit = template.unit,
            notes = template.notes
        )
        
        viewModelScope.launch {
            repository.updateTemplateUsage(template.id)
        }
    }
}