package xyz.moroku0519.shoppinghelper.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import xyz.moroku0519.shoppinghelper.database.ShoppingDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            ShoppingDatabase.Schema,
            "shopping.db"
        )
    }
}