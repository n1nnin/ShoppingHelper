package xyz.moroku0519.shoppinghelper.di

import org.koin.dsl.module
import xyz.moroku0519.shoppinghelper.data.repository.AndroidShoppingRepository
import xyz.moroku0519.shoppinghelper.data.repository.ShoppingRepository

val androidModule = module {
    single<ShoppingRepository> { AndroidShoppingRepository(get()) }
}