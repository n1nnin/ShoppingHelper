package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.moroku0519.shoppinghelper.domain.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.components.AddItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShoppingItemCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateToShops: () -> Unit = {}
) {
    var itemToDelete by remember { mutableStateOf<ShoppingItemUi?>(null) }
    var items by remember {
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
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("買い物リスト")
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
                actions = {
                    IconButton(onClick = onNavigateToShops) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "お店一覧")
                    }
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
                    println("アイテム編集: $id")
                },
                onDeleteItem = { id ->
                    itemToDelete = items.firstOrNull { it.id == id }
                }
            )

            // アイテム追加ダイアログ
            AddItemDialog(
                isVisible = showAddDialog,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, shopName, priority ->
                    // 新しいアイテムを追加
                    val newItem = ShoppingItemUi(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        isCompleted = false,
                        shopName = shopName,
                        shopId = shopName?.let { "shop_${UUID.randomUUID()}" },
                        priority = priority
                    )
                    items = items + newItem

                    println("新しいアイテムが追加されました: $newItem")
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