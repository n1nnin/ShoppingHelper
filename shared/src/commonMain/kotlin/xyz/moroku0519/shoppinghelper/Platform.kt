package xyz.moroku0519.shoppinghelper

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform