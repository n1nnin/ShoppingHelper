package xyz.moroku0519.shoppinghelper.data.migration

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase
import xyz.moroku0519.shoppinghelper.model.*
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis

class DataMigrationHelper(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("shopping_data", Context.MODE_PRIVATE)
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    suspend fun migrateFromSharedPreferences(database: ShoppingDatabase): Boolean {
        try {
            // Check if migration is needed
            if (!needsMigration()) {
                return false
            }
            
            // Start transaction
            database.transaction {
                // Migrate Lists
                migrateLists(database)
                
                // Migrate Shops
                migrateShops(database)
                
                // Migrate Items
                migrateItems(database)
                
                // Migrate Templates
                migrateTemplates(database)
            }
            
            // Mark migration as complete
            markMigrationComplete()
            
            // Optional: Clear old SharedPreferences data
            // clearOldData()
            
            return true
        } catch (e: Exception) {
            println("Migration failed: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    private fun needsMigration(): Boolean {
        // Check if migration has already been done
        val migrationDone = prefs.getBoolean("migration_to_sqldelight_done", false)
        if (migrationDone) return false
        
        // Check if there's data to migrate
        val hasLists = prefs.getString("lists", null) != null
        val hasShops = prefs.getString("shops", null) != null
        val hasItems = prefs.getString("items", null) != null
        
        return hasLists || hasShops || hasItems
    }
    
    private fun migrateLists(database: ShoppingDatabase) {
        val listsJson = prefs.getString("lists", "[]") ?: "[]"
        try {
            val lists = json.decodeFromString<List<ShoppingList>>(listsJson)
            
            lists.forEach { list ->
                database.shoppingListQueries.insertList(
                    id = list.id,
                    name = list.name,
                    is_active = if (list.isActive) 1L else 0L,
                    created_at = list.createdAt,
                    updated_at = list.updatedAt
                )
            }
            
            println("Migrated ${lists.size} shopping lists")
        } catch (e: Exception) {
            println("Failed to migrate lists: ${e.message}")
        }
    }
    
    private fun migrateShops(database: ShoppingDatabase) {
        val shopsJson = prefs.getString("shops", "[]") ?: "[]"
        try {
            val shops = json.decodeFromString<List<Shop>>(shopsJson)
            
            shops.forEach { shop ->
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
            
            println("Migrated ${shops.size} shops")
        } catch (e: Exception) {
            println("Failed to migrate shops: ${e.message}")
        }
    }
    
    private fun migrateItems(database: ShoppingDatabase) {
        val itemsJson = prefs.getString("items", "[]") ?: "[]"
        try {
            val items = json.decodeFromString<List<ShoppingItem>>(itemsJson)
            
            items.forEach { item ->
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
            
            println("Migrated ${items.size} shopping items")
        } catch (e: Exception) {
            println("Failed to migrate items: ${e.message}")
        }
    }
    
    private fun migrateTemplates(database: ShoppingDatabase) {
        val templatesJson = prefs.getString("templates", "[]") ?: "[]"
        try {
            val templates = json.decodeFromString<List<ItemTemplate>>(templatesJson)
            
            templates.forEach { template ->
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
            
            println("Migrated ${templates.size} item templates")
        } catch (e: Exception) {
            println("Failed to migrate templates: ${e.message}")
        }
    }
    
    private fun markMigrationComplete() {
        prefs.edit()
            .putBoolean("migration_to_sqldelight_done", true)
            .putLong("migration_timestamp", currentTimeMillis())
            .apply()
    }
    
    private fun clearOldData() {
        // Optional: Remove old SharedPreferences data after successful migration
        prefs.edit()
            .remove("lists")
            .remove("shops")
            .remove("items")
            .remove("templates")
            .apply()
    }
}