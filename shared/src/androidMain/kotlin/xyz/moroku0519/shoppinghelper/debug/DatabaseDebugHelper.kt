package xyz.moroku0519.shoppinghelper.debug

import android.content.Context
import android.util.Log
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase
import java.io.File

class DatabaseDebugHelper(
    private val context: Context,
    private val database: ShoppingDatabase
) {
    companion object {
        private const val TAG = "DatabaseDebug"
    }
    
    fun printDatabaseStats() {
        try {
            Log.d(TAG, "=== Database Statistics ===")
            
            val lists = database.shoppingListQueries.getAllLists().executeAsList()
            Log.d(TAG, "Shopping Lists: ${lists.size}")
            lists.forEach { list ->
                Log.d(TAG, "  - ${list.name} (active: ${list.is_active == 1L})")
            }
            
            val items = database.shoppingItemQueries.getAllIncompleteItems().executeAsList()
            Log.d(TAG, "Incomplete Items: ${items.size}")
            
            val shops = database.shopQueries.getAllShops().executeAsList()
            Log.d(TAG, "Shops: ${shops.size}")
            
            val templates = database.itemTemplateQueries.getAllTemplates().executeAsList()
            Log.d(TAG, "Templates: ${templates.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading database stats", e)
        }
    }
    
    fun exportDatabaseFile(): String? {
        return try {
            val dbFile = context.getDatabasePath("shopping.db")
            val exportFile = File(context.getExternalFilesDir(null), "shopping_export.db")
            
            if (dbFile.exists()) {
                dbFile.copyTo(exportFile, overwrite = true)
                val path = exportFile.absolutePath
                Log.d(TAG, "Database exported to: $path")
                path
            } else {
                Log.w(TAG, "Database file not found at: ${dbFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database", e)
            null
        }
    }
    
    fun testBasicQueries() {
        Log.d(TAG, "=== Testing Basic Queries ===")
        
        try {
            // Test list queries
            val allLists = database.shoppingListQueries.getAllLists().executeAsList()
            Log.d(TAG, "getAllLists() returned ${allLists.size} items")
            
            val activeList = database.shoppingListQueries.getActiveList().executeAsOneOrNull()
            Log.d(TAG, "getActiveList() returned: ${activeList?.name ?: "null"}")
            
            // Test item queries if we have a list
            if (allLists.isNotEmpty()) {
                val listId = allLists.first().id
                val items = database.shoppingItemQueries.getItemsByListId(listId).executeAsList()
                Log.d(TAG, "getItemsByListId($listId) returned ${items.size} items")
            }
            
            // Test shop queries
            val allShops = database.shopQueries.getAllShops().executeAsList()
            Log.d(TAG, "getAllShops() returned ${allShops.size} items")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error testing queries", e)
        }
    }
    
    fun validateDatabaseIntegrity() {
        Log.d(TAG, "=== Database Integrity Check ===")
        
        try {
            // Check for orphaned items
            val allItems = database.shoppingItemQueries.getAllIncompleteItems().executeAsList()
            val allLists = database.shoppingListQueries.getAllLists().executeAsList()
            val listIds = allLists.map { it.id }.toSet()
            
            val orphanedItems = allItems.filter { it.list_id !in listIds }
            if (orphanedItems.isNotEmpty()) {
                Log.w(TAG, "Found ${orphanedItems.size} orphaned items")
                orphanedItems.forEach { item ->
                    Log.w(TAG, "  - Item '${item.name}' references non-existent list '${item.list_id}'")
                }
            } else {
                Log.d(TAG, "No orphaned items found")
            }
            
            // Check for items with invalid shop references
            val allShops = database.shopQueries.getAllShops().executeAsList()
            val shopIds = allShops.map { it.id }.toSet()
            
            val itemsWithInvalidShops = allItems.filter { 
                it.shop_id != null && it.shop_id !in shopIds 
            }
            if (itemsWithInvalidShops.isNotEmpty()) {
                Log.w(TAG, "Found ${itemsWithInvalidShops.size} items with invalid shop references")
            } else {
                Log.d(TAG, "All shop references are valid")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating database integrity", e)
        }
    }
}