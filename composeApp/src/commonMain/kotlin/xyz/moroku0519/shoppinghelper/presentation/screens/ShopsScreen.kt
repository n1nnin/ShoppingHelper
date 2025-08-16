package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.components.AddShopDialog
import xyz.moroku0519.shoppinghelper.presentation.components.EditShopDialog
import xyz.moroku0519.shoppinghelper.presentation.components.GeofenceHandler
import xyz.moroku0519.shoppinghelper.presentation.components.GeofenceTestButton
import xyz.moroku0519.shoppinghelper.presentation.components.ShopCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import xyz.moroku0519.shoppinghelper.util.currentTimeMillis
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(
    initialShops: List<ShopUi> = emptyList(),
    onShopsUpdated: (List<ShopUi>) -> Unit = {},
    onBackClick: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onShopClick: (String) -> Unit = {}
) {
    // 状態管理：お店リスト
    var shops by remember { mutableStateOf(initialShops) }

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
                    // 新しいお店を追加
                    val newShop = Shop(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        address = address,
                        category = category,
                        longitude = randomLng,
                        latitude = randomLat,
                        createdAt = currentTimeMillis()
                    ).toUiModel()

                    shops = shops + newShop
                    onShopsUpdated(shops)
                    showAddDialog = false

                    println("新しいお店が追加されました: $newShop")
                }
            )

            // お店編集ダイアログ
            EditShopDialog(
                shop = shopToEdit,
                onDismiss = { shopToEdit = null },
                onConfirm = { name, address, category ->
                    shops = shops.map { shop ->
                        if (shop.id == shopToEdit?.id) {
                            Shop(
                                id = shop.id,
                                name = name,
                                address = address,
                                category = category,
                                latitude = shop.latitude,
                                longitude = shop.longitude
                            ).toUiModel(
                                pendingItemsCount = shop.pendingItemsCount,
                                totalItemsCount = shop.totalItemsCount
                            )
                        } else {
                            shop
                        }
                    }
                    shopToEdit = null
                    onShopsUpdated(shops)
                    println("お店が更新されました: ${shopToEdit?.name}")
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
                                shops = shops.filter { it.id != shop.id }
                                onShopsUpdated(shops)
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