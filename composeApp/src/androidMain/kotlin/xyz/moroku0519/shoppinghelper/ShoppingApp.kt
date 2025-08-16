package xyz.moroku0519.shoppinghelper

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.moroku0519.shoppinghelper.di.androidModule
import xyz.moroku0519.shoppinghelper.di.sharedModule

class ShoppingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ShoppingApp)
            modules(
                sharedModule,
                androidModule
            )
        }
    }
}