package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import org.koin.compose.koinInject
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel
import xyz.moroku0519.shoppinghelper.presentation.debug.SupabaseTestScreen
import xyz.moroku0519.shoppinghelper.presentation.debug.DebugMenuScreen
import xyz.moroku0519.shoppinghelper.BuildConfig

sealed class BottomNavScreen(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    data object Shops : BottomNavScreen(
        route = "shops",
        icon = Icons.Default.ShoppingCart,
        label = "お店"
    )
    
    data object Lists : BottomNavScreen(
        route = "lists",
        icon = Icons.AutoMirrored.Filled.List,
        label = "リスト"
    )
    
    data object Map : BottomNavScreen(
        route = "map",
        icon = Icons.Default.LocationOn,
        label = "地図"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: ShoppingListViewModel = koinInject()
    
    // 現在の画面を追跡
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // ボトムナビゲーションに表示する画面
    val bottomNavItems = listOf(
        BottomNavScreen.Shops,
        BottomNavScreen.Lists,
        BottomNavScreen.Map
    )
    
    // ボトムナビゲーションを表示するかどうか
    val shouldShowBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // 同じタブを複数回選択しても重複しないようにする
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 選択された画面の状態を復元
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // デバッグビルドでのみFloatingActionButtonを表示
            if (BuildConfig.DEBUG && shouldShowBottomBar) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("debug_menu")
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Build, contentDescription = "デバッグメニュー")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Shops.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // お店タブのナビゲーション
            composable(BottomNavScreen.Shops.route) {
                ShopsNavigation()
            }
            
            // リストタブのナビゲーション
            composable(BottomNavScreen.Lists.route) {
                ListsNavigation()
            }
            
            // 地図タブ
            composable(BottomNavScreen.Map.route) {
                MapScreen(
                    onBackClick = {} // ボトムナビゲーションなので戻るボタンは不要
                )
            }
            
            // デバッグメニュー（デバッグビルドのみ）
            if (BuildConfig.DEBUG) {
                composable("debug_menu") {
                    DebugMenuScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToSupabaseTest = {
                            navController.navigate("supabase_test")
                        }
                    )
                }
                
                composable("supabase_test") {
                    SupabaseTestScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

// お店タブ内のナビゲーション
@Composable
fun ShopsNavigation() {
    val navController = rememberNavController()
    val viewModel: ShoppingListViewModel = koinInject()
    
    NavHost(
        navController = navController,
        startDestination = "shops_list"
    ) {
        composable("shops_list") {
            val shops by viewModel.shops.collectAsState()
            ShopsScreen(
                initialShops = shops,
                onShopsUpdated = { /* ViewModelで管理 */ },
                onBackClick = { /* メイン画面なので不要 */ },
                onNavigateToMap = { /* ボトムナビで遷移 */ },
                onShopClick = { shopId ->
                    navController.navigate("shop_items/$shopId")
                }
            )
        }
        
        composable(
            route = "shop_items/{shopId}",
            arguments = listOf(
                navArgument("shopId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId")
            val shops by viewModel.shops.collectAsState()
            
            ShoppingListScreen(
                shopId = shopId,
                shops = shops,
                onNavigateToShops = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// リストタブ内のナビゲーション
@Composable
fun ListsNavigation() {
    val navController = rememberNavController()
    val viewModel: ShoppingListViewModel = koinInject()
    
    NavHost(
        navController = navController,
        startDestination = "lists_management"
    ) {
        composable("lists_management") {
            ShoppingListManagementScreen(
                onBackClick = { /* メイン画面なので不要 */ },
                onListSelected = { listId ->
                    navController.navigate("list_items/$listId")
                }
            )
        }
        
        composable(
            route = "list_items/{listId}",
            arguments = listOf(
                navArgument("listId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            
            // リスト視点での買い物リスト画面（全お店横断）
            ListItemsScreen(
                listId = listId ?: "",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}