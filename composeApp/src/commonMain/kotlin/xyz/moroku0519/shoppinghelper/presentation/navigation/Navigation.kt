package xyz.moroku0519.shoppinghelper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.moroku0519.shoppinghelper.presentation.screens.ShoppingListScreen
import xyz.moroku0519.shoppinghelper.presentation.screens.ShopsScreen

// 画面定義
sealed class Screen(val route: String) {
    data object ShoppingList : Screen("shopping_list")
    data object Shops : Screen("shops")
}

@Composable
fun ShoppingMemoNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ShoppingList.route
    ) {
        // 買い物リスト画面
        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onNavigateToShops = {
                    navController.navigate(Screen.Shops.route)
                }
            )
        }

        // お店一覧画面
        composable(Screen.Shops.route) {
            ShopsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}