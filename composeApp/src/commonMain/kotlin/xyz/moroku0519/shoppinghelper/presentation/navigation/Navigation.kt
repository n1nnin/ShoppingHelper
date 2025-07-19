package xyz.moroku0519.shoppinghelper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import xyz.moroku0519.shoppinghelper.presentation.screens.MapScreen
import xyz.moroku0519.shoppinghelper.presentation.screens.ShoppingListScreen
import xyz.moroku0519.shoppinghelper.presentation.screens.ShopsScreen

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
}

@Composable
fun ShoppingMemoNavigation(
    navController: NavHostController = rememberNavController()
) {
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
            ShopsScreen(
                onBackClick = {
                    // メイン画面なので戻るボタンは不要
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onShopClick = { shopId ->
                    navController.navigate(Screen.ShoppingList.createRoute(shopId))
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}