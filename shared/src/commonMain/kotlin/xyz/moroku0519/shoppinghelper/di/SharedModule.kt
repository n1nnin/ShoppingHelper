package xyz.moroku0519.shoppinghelper.di

import org.koin.dsl.module
import xyz.moroku0519.shoppinghelper.data.repository.ShoppingRepository
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel

val sharedModule = module {
    // Repository - platform-specific implementation will be provided
    // Android: AndroidShoppingRepository
    // iOS: InMemoryShoppingRepository (for now)
    
    // ViewModels
    factory { ShoppingListViewModel(get<ShoppingRepository>()) }
}