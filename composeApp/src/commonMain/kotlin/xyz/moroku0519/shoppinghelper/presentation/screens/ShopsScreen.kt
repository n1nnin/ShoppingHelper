package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.model.Location
import xyz.moroku0519.shoppinghelper.presentation.components.AddShopDialog
import xyz.moroku0519.shoppinghelper.presentation.components.EditShopDialog
import xyz.moroku0519.shoppinghelper.presentation.components.GeofenceHandler
import xyz.moroku0519.shoppinghelper.presentation.components.GeofenceTestButton
import xyz.moroku0519.shoppinghelper.presentation.components.ShopCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis
import xyz.moroku0519.shoppinghelper.BuildConfig
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(
    initialShops: List<ShopUi> = emptyList(),
    onShopsUpdated: (List<ShopUi>) -> Unit = {},
    onBackClick: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onShopClick: (String) -> Unit = {},
    onNavigateToSupabaseTest: (() -> Unit)? = null
) {
    val viewModel: ShoppingListViewModel = koinInject()
    
    // ViewModelからお店データを取得
    val shops by viewModel.shops.collectAsState()
    
    // 初期データがある場合は無視（ViewModelのデータを優先）

    // Geofence自動設定
    GeofenceHandler(shops)

    // 削除確認ダイアログ用の状態
    var shopToDelete by remember { mutableStateOf<ShopUi?>(null) }
    var shopToEdit by remember { mutableStateOf<ShopUi?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("お店一覧")
                        Text(
                            text = "${shops.size}件のお店",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    // お店一覧がメイン画面になったため、戻るボタンは不要
                },
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "地図表示"
                        )
                    }
                    
                    // デバッグビルドのみSupabaseテストボタンを表示
                    if (BuildConfig.DEBUG && onNavigateToSupabaseTest != null) {
                        IconButton(onClick = onNavigateToSupabaseTest) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Supabaseテスト"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "お店追加")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // テスト用：常にSupabaseテストボタンを表示（一時的）
            println("🧪 DEBUG CHECK: BuildConfig.DEBUG=${BuildConfig.DEBUG}, onNavigateToSupabaseTest=${onNavigateToSupabaseTest != null}")
            // 一時的にtrueに固定してテスト
            if (true) {
                println("🧪 DEBUG: Showing Supabase test button - BuildConfig.DEBUG=${BuildConfig.DEBUG}, onNavigateToSupabaseTest=${onNavigateToSupabaseTest != null}")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🧪 Supabase接続テスト",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "デバッグ機能：データベース接続と認証をテスト",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(onClick = {
                            onNavigateToSupabaseTest?.invoke() ?: run {
                                println("🧪 DEBUG: onNavigateToSupabaseTest is null!")
                            }
                        }) {
                            Text("テスト実行")
                        }
                    }
                }
            }
            
            if (shops.isEmpty()) {
                // 空状態
                EmptyShopsState()
            } else {
                // お店リスト
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = shops,
                        key = { it.id }
                    ) { shop ->
                        ShopCard(
                            shop = shop,
                            onShopClick = {
                                onShopClick(shop.id)
                            },
                            onEditClick = {
                                shopToEdit = shop
                            },
                            onDeleteClick = {
                                shopToDelete = shop
                            }
                        )
                    }
                    
                    // Geofence通知テストボタンを追加
                    item {
                        GeofenceTestButton(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            AddShopDialog(
                isVisible = showAddDialog,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, address, category ->
                    val randomLat = 35.6812 + (Math.random() - 0.5) * 0.01
                    val randomLng = 139.7671 + (Math.random() - 0.5) * 0.01
                    
                    // ViewModelを使用してお店を追加（永続化される）
                    viewModel.addShop(
                        name = name,
                        address = address,
                        latitude = randomLat,
                        longitude = randomLng,
                        category = category
                    )
                    
                    showAddDialog = false
                    println("新しいお店が追加されました: $name")
                }
            )

            // お店編集ダイアログ
            EditShopDialog(
                shop = shopToEdit,
                onDismiss = { shopToEdit = null },
                onConfirm = { name, address, category ->
                    shopToEdit?.let { shop ->
                        // ViewModelを使用してお店を更新（永続化される）
                        viewModel.updateShop(
                            shopId = shop.id,
                            name = name,
                            address = address,
                            latitude = shop.latitude,
                            longitude = shop.longitude,
                            category = category
                        )
                    }
                    shopToEdit = null
                    println("お店が更新されました")
                }
            )

            // 削除確認ダイアログ
            shopToDelete?.let { shop ->
                AlertDialog(
                    onDismissRequest = { shopToDelete = null },
                    title = { Text("お店を削除") },
                    text = {
                        Text(
                            "「${shop.name}」を削除しますか？\n" +
                                    if (shop.totalItemsCount > 0)
                                        "このお店に関連する${shop.totalItemsCount}件のアイテムも削除されます。"
                                    else ""
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // ViewModelを使用してお店を削除（永続化される）
                                viewModel.deleteShop(shop.id)
                                shopToDelete = null
                                println("お店削除: ${shop.name}")
                            }
                        ) {
                            Text("削除", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { shopToDelete = null }) {
                            Text("キャンセル")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyShopsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "お店が登録されていません",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "右下の+ボタンでお店を追加してください",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun ShopsScreenWithShopsPreview() {
    MaterialTheme {
        ShopsScreen(
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ShopsScreenEmptyPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("お店一覧")
                            Text(
                                text = "0件のお店",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "戻る"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "お店追加")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyShopsState()
            }
        }
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("お店を削除") },
            text = {
                Text(
                    "「イオン渋谷店」を削除しますか？\n" +
                            "このお店に関連する8件のアイテムも削除されます。"
                )
            },
            confirmButton = {
                TextButton(onClick = {}) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("キャンセル")
                }
            }
        )
    }
}