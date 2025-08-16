package xyz.moroku0519.shoppinghelper.di

import org.koin.dsl.module
import xyz.moroku0519.shoppinghelper.data.repository.InMemoryShoppingRepository
import xyz.moroku0519.shoppinghelper.data.repository.ShoppingRepository

val iosModule = module {
    single<ShoppingRepository> { InMemoryShoppingRepository() }
}