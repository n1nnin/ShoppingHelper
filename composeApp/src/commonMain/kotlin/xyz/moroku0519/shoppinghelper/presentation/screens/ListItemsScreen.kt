package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import xyz.moroku0519.shoppinghelper.model.ItemCategory
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.components.AddItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.EditItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShoppingItemCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemsScreen(
    listId: String,
    onBackClick: () -> Unit
) {
    val viewModel: ShoppingListViewModel = koinInject()
    
    // リスト情報を取得
    val allLists by viewModel.allLists.collectAsState()
    val currentList = allLists.firstOrNull { it.id == listId }
    
    // このリストのアイテムを取得
    LaunchedEffect(listId) {
        viewModel.selectList(listId)
    }
    val items by viewModel.currentListItems.collectAsState()
    val shops by viewModel.shops.collectAsState()
    
    // お店ごとにアイテムをグループ化
    val itemsByShop = items.groupBy { item ->
        shops.firstOrNull { it.id == item.shopId }
    }.toSortedMap(compareBy { it?.name ?: "お店未指定" })
    
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ShoppingItemUi?>(null) }
    var itemToEdit by remember { mutableStateOf<ShoppingItemUi?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentList?.name ?: "買い物リスト",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // 統計情報
                        val completedCount = items.count { it.isCompleted }
                        val totalCount = items.size
                        if (totalCount > 0) {
                            Text(
                                text = "$completedCount/$totalCount 完了 • ${itemsByShop.size}店舗",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    // 完了アイテムを削除
                    if (items.any { it.isCompleted }) {
                        IconButton(
                            onClick = {
                                viewModel.deleteCompletedItems()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "完了アイテムを削除"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "アイテム追加")
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            EmptyListState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // お店ごとにセクション分け
                itemsByShop.forEach { (shop, shopItems) ->
                    item {
                        ShopSection(
                            shopName = shop?.name ?: "お店未指定",
                            shopColor = shop?.category?.color ?: MaterialTheme.colorScheme.outline,
                            itemCount = shopItems.size,
                            completedCount = shopItems.count { it.isCompleted }
                        )
                    }
                    
                    items(
                        items = shopItems,
                        key = { it.id }
                    ) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggle = { viewModel.toggleItemComplete(item.id) },
                            onEdit = { itemToEdit = item },
                            onDelete = { itemToDelete = item },
                            showShopName = false // お店セクション内なので表示不要
                        )
                    }
                }
            }
        }
        
        // アイテム追加ダイアログ（リスト用）
        AddItemDialog(
            isVisible = showAddDialog,
            currentShopId = null, // リスト視点では店舗指定なし
            availableShops = shops, // お店選択可能
            onDismiss = { showAddDialog = false },
            onConfirm = { name, shopId, priority, category ->
                viewModel.addItem(
                    name = name,
                    shopId = shopId,
                    priority = priority,
                    category = category
                )
                showAddDialog = false
            }
        )
        
        // アイテム編集ダイアログ
        EditItemDialog(
            item = itemToEdit,
            availableShops = shops, // お店選択可能
            onDismiss = { itemToEdit = null },
            onConfirm = { name, shopId, priority ->
                itemToEdit?.let { item ->
                    viewModel.updateItem(
                        itemId = item.id,
                        name = name,
                        shopId = shopId,
                        priority = priority,
                        category = item.category
                    )
                }
                itemToEdit = null
            }
        )
        
        // 削除確認ダイアログ
        itemToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("削除確認") },
                text = { Text("「${item.name}」を削除しますか？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(item.id)
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

@Composable
private fun ShopSection(
    shopName: String,
    shopColor: androidx.compose.ui.graphics.Color,
    itemCount: Int,
    completedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // お店のカラーインジケーター
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = shopColor,
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // お店名
        Text(
            text = shopName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        // アイテム数
        Text(
            text = "$completedCount/$itemCount",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyListState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "このリストにアイテムがありません",
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