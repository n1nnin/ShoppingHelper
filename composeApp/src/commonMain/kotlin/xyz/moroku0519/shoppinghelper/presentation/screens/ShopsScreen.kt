package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.domain.model.Shop
import xyz.moroku0519.shoppinghelper.domain.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.components.AddShopDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShopCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsScreen(
    onBackClick: () -> Unit,
    onNavigateToMap: () -> Unit = {}
) {
    // 状態管理：お店リスト
    var shops by remember {
        mutableStateOf(
            listOf(
                Shop(
                    id = "shop1",
                    name = "イオン",
                    address = "東京都渋谷区神南1-1-1",
                    category = ShopCategory.GROCERY,
                    latitude = 35.6598, longitude = 139.7006
                ).toUiModel(pendingItemsCount = 3, totalItemsCount = 8),
                Shop(
                    id = "shop2",
                    name = "ツルハドラッグ",
                    address = "東京都新宿区新宿3-1-1",
                    category = ShopCategory.PHARMACY,
                    latitude = 35.6896, longitude = 139.7006
                ).toUiModel(pendingItemsCount = 1, totalItemsCount = 2),
                Shop(
                    id = "shop3",
                    name = "セブンイレブン",
                    address = "東京都千代田区丸の内1-1-1",
                    category = ShopCategory.CONVENIENCE,
                    latitude = 35.6812, longitude = 139.7671
                ).toUiModel(pendingItemsCount = 0, totalItemsCount = 1)
            )
        )
    }

    // 削除確認ダイアログ用の状態
    var shopToDelete by remember { mutableStateOf<ShopUi?>(null) }
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
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
                                println("お店クリック: ${shop.name}")
                                // TODO: お店詳細画面への遷移
                            },
                            onEditClick = {
                                println("お店編集: ${shop.name}")
                                // TODO: お店編集ダイアログを表示
                            },
                            onDeleteClick = {
                                shopToDelete = shop
                            }
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
                        createdAt = System.currentTimeMillis()
                    ).toUiModel()

                    shops = shops + newShop
                    showAddDialog = false

                    println("新しいお店が追加されました: $newShop")
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