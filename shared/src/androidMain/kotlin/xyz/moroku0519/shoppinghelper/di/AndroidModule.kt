package xyz.moroku0519.shoppinghelper.di

import app.cash.sqldelight.ColumnAdapter
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import xyz.moroku0519.shoppinghelper.data.database.DatabaseDriverFactory
import xyz.moroku0519.shoppinghelper.data.migration.DataMigrationHelper
import xyz.moroku0519.shoppinghelper.data.repository.ShoppingRepository
import xyz.moroku0519.shoppinghelper.data.repository.SqlDelightShoppingRepository
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase
import xyz.moroku0519.shoppinghelper.database.Shopping_item
import xyz.moroku0519.shoppinghelper.database.Shop
import xyz.moroku0519.shoppinghelper.database.Item_template
import xyz.moroku0519.shoppinghelper.model.*

val androidModule = module {
    single { DatabaseDriverFactory(get()) }
    
    single { 
        val driver = get<DatabaseDriverFactory>().createDriver()
        
        // Create enum adapters
        val priorityAdapter = object : ColumnAdapter<Priority, String> {
            override fun decode(databaseValue: String) = Priority.valueOf(databaseValue)
            override fun encode(value: Priority) = value.name
        }
        
        val itemCategoryAdapter = object : ColumnAdapter<ItemCategory, String> {
            override fun decode(databaseValue: String) = ItemCategory.valueOf(databaseValue)
            override fun encode(value: ItemCategory) = value.name
        }
        
        val shopCategoryAdapter = object : ColumnAdapter<ShopCategory, String> {
            override fun decode(databaseValue: String) = ShopCategory.valueOf(databaseValue)
            override fun encode(value: ShopCategory) = value.name
        }
        
        ShoppingDatabase(
            driver = driver,
            shopping_itemAdapter = Shopping_item.Adapter(
                priorityAdapter = priorityAdapter,
                categoryAdapter = itemCategoryAdapter
            ),
            shopAdapter = Shop.Adapter(
                categoryAdapter = shopCategoryAdapter
            ),
            item_templateAdapter = Item_template.Adapter(
                categoryAdapter = itemCategoryAdapter
            )
        )
    }
    
    single { DataMigrationHelper(get()) }
    
    single<ShoppingRepository> { 
        val database = get<ShoppingDatabase>()
        val migrationHelper = get<DataMigrationHelper>()
        
        // Run migration on first launch
        runBlocking {
            val migrated = migrationHelper.migrateFromSharedPreferences(database)
            if (migrated) {
                println("Successfully migrated data from SharedPreferences to SQLDelight")
            }
        }
        
        SqlDelightShoppingRepository(database)
    }
}