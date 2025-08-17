package xyz.moroku0519.shoppinghelper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.model.Location
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import xyz.moroku0519.shoppinghelper.presentation.screens.MapScreen
import xyz.moroku0519.shoppinghelper.presentation.screens.ShoppingListScreen
import xyz.moroku0519.shoppinghelper.presentation.screens.ShopsScreen
import xyz.moroku0519.shoppinghelper.presentation.debug.SupabaseTestScreen
import xyz.moroku0519.shoppinghelper.BuildConfig

// 画面定義
sealed class Screen(val route: String) {
    data object ShoppingList : Screen("shopping_list/{shopId}") {
        fun createRoute(shopId: String? = null): String {
            return if (shopId != null) {
                "shopping_list/$shopId"
            } else {
                "shopping_list/all"
            }
        }
    }
    data object Shops : Screen("shops")
    data object Map : Screen("map")
    data object SupabaseTest : Screen("supabase_test")
}

@Composable
fun ShoppingMemoNavigation(
    navController: NavHostController = rememberNavController()
) {
    // お店のデータを共有
    val shops = remember {
        mutableStateOf(
            listOf(
                Shop(
                    id = "shop1",
                    name = "イオン",
                    address = "東京都渋谷区神南１－１－１",
                    location = Location(35.6598, 139.7006),
                    category = ShopCategory.GROCERY
                ).toUiModel(pendingItemsCount = 3, totalItemsCount = 8),
                Shop(
                    id = "shop2",
                    name = "ツルハドラッグ",
                    address = "東京都新宿区新宿３－１－１",
                    location = Location(35.6896, 139.7006),
                    category = ShopCategory.PHARMACY
                ).toUiModel(pendingItemsCount = 1, totalItemsCount = 2),
                Shop(
                    id = "shop3",
                    name = "セブンイレブン",
                    address = "東京都千代田区丸の内１－１－１",
                    location = Location(35.6812, 139.7671),
                    category = ShopCategory.CONVENIENCE
                ).toUiModel(pendingItemsCount = 0, totalItemsCount = 1)
            )
        )
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Shops.route
    ) {
        // 買い物リスト画面
        composable(
            route = Screen.ShoppingList.route,
            arguments = listOf(
                navArgument("shopId") {
                    type = NavType.StringType
                    defaultValue = "all"
                }
            )
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId")
            ShoppingListScreen(
                shopId = if (shopId == "all") null else shopId,
                shops = shops.value,
                onNavigateToShops = {
                    navController.navigate(Screen.Shops.route)
                },
                onBackClick = if (shopId != "all") {
                    { navController.popBackStack() }
                } else null
            )
        }

        // お店一覧画面（メイン画面）
        composable(Screen.Shops.route) {
            val supabaseTestCallback = { 
                println("🧪 DEBUG NAV: Navigating to Supabase test")
                navController.navigate(Screen.SupabaseTest.route) 
            }
            println("🧪 DEBUG NAV: Creating ShopsScreen with supabaseTestCallback = $supabaseTestCallback")
            
            ShopsScreen(
                initialShops = shops.value,
                onShopsUpdated = { updatedShops ->
                    shops.value = updatedShops
                },
                onBackClick = {
                    // メイン画面なので戻るボタンは不要
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onShopClick = { shopId ->
                    navController.navigate(Screen.ShoppingList.createRoute(shopId))
                },
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Supabaseテスト画面（デバッグビルドのみ）
        if (BuildConfig.DEBUG) {
            composable(Screen.SupabaseTest.route) {
                SupabaseTestScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}