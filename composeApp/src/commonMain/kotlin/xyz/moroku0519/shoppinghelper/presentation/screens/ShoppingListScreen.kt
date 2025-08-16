package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.components.AddItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.EditItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShoppingItemCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    shopId: String? = null,
    shops: List<ShopUi> = emptyList(),
    onNavigateToShops: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    var itemToDelete by remember { mutableStateOf<ShoppingItemUi?>(null) }
    var itemToEdit by remember { mutableStateOf<ShoppingItemUi?>(null) }
    
    // 全アイテムを保持
    val allItems = remember {
        mutableStateOf(
            listOf(
                ShoppingItemUi(
                    id = "1",
                    name = "牛乳",
                    isCompleted = false,
                    shopName = "スーパーマーケット",
                    shopId = "shop1",
                    priority = Priority.NORMAL
                ),
                ShoppingItemUi(
                    id = "2",
                    name = "パン",
                    isCompleted = true,
                    shopName = "ベーカリー",
                    shopId = "shop2",
                    priority = Priority.HIGH
                ),
                ShoppingItemUi(
                    id = "3",
                    name = "卵",
                    isCompleted = false,
                    shopName = null,
                    shopId = null,
                    priority = Priority.URGENT
                ),
                ShoppingItemUi(
                    id = "4",
                    name = "りんご",
                    isCompleted = false,
                    shopName = "フルーツショップ",
                    shopId = "shop3",
                    priority = Priority.LOW
                )
            )
        )
    }.value
    
    // shopIdに基づいてフィルタリング
    var items by remember(shopId) {
        mutableStateOf(
            if (shopId != null) {
                allItems.filter { it.shopId == shopId }
            } else {
                allItems
            }
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (shopId != null) {
                                val shopName = shops.firstOrNull { it.id == shopId }?.name ?: "お店"
                                "${shopName}の買い物リスト"
                            } else {
                                "買い物リスト"
                            }
                        )
                        // 統計情報を表示
                        val completedCount = items.count { it.isCompleted }
                        val totalCount = items.size
                        if (totalCount > 0) {
                            Text(
                                text = "$completedCount/$totalCount 完了",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick?.invoke() ?: onNavigateToShops() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    // お店一覧への直接遷移は削除（戻るボタンで戻るため）
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true  // ダイアログ表示
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // アイテムリスト
            ShoppingListContent(
                items = items,
                onToggleItem = { id ->
                    items = items.map { item ->
                        if (item.id == id) {
                            item.copy(isCompleted = !item.isCompleted)
                        } else {
                            item
                        }
                    }
                },
                onEditItem = { id ->
                    itemToEdit = items.firstOrNull { it.id == id }
                },
                onDeleteItem = { id ->
                    itemToDelete = items.firstOrNull { it.id == id }
                }
            )

            // アイテム追加ダイアログ
            AddItemDialog(
                isVisible = showAddDialog,
                shops = shops,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, selectedShopId, priority ->
                    // 選択されたお店の情報を取得
                    val selectedShop = shops.firstOrNull { it.id == selectedShopId }
                    
                    // 新しいアイテムを追加
                    val newItem = ShoppingItemUi(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        isCompleted = false,
                        shopName = selectedShop?.name,
                        shopId = selectedShopId,
                        priority = priority
                    )
                    items = items + newItem

                    println("新しいアイテムが追加されました: $newItem")
                }
            )

            // アイテム編集ダイアログ
            EditItemDialog(
                item = itemToEdit,
                shops = shops,
                onDismiss = { itemToEdit = null },
                onConfirm = { name, selectedShopId, priority ->
                    // 選択されたお店の情報を取得
                    val selectedShop = shops.firstOrNull { it.id == selectedShopId }
                    
                    items = items.map { item ->
                        if (item.id == itemToEdit?.id) {
                            item.copy(
                                name = name,
                                shopName = selectedShop?.name,
                                shopId = selectedShopId,
                                priority = priority
                            )
                        } else {
                            item
                        }
                    }
                    itemToEdit = null
                    println("アイテムが更新されました: ${itemToEdit?.id}")
                }
            )

            itemToDelete?.let { item ->
                AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    title = { Text("削除確認") },
                    text = { Text("「${item.name}」を削除しますか？") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                items = items.filter { it.id != item.id }
                                itemToDelete = null
                            }
                        ) {
                            Text("削除")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemToDelete = null }) {
                            Text("キャンセル")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    items: List<ShoppingItemUi>,
    onToggleItem: (String) -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        // 空状態の表示
        EmptyState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = items,
                key = { it.id }
            ) { item ->
                ShoppingItemCard(
                    item = item,
                    onToggle = { onToggleItem(item.id) },
                    onEdit = { onEditItem(item.id) },
                    onDelete = { onDeleteItem(item.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "買い物リストが空です",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "右下の+ボタンでアイテムを追加してください",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}