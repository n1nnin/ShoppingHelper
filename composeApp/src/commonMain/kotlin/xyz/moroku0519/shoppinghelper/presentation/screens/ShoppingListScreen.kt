package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import xyz.moroku0519.shoppinghelper.model.ItemCategory
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.components.AddItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.EditItemDialog
import xyz.moroku0519.shoppinghelper.presentation.components.ShoppingItemCard
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.viewmodel.ShoppingListViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    shopId: String? = null,
    shops: List<ShopUi> = emptyList(),
    onNavigateToShops: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    val viewModel: ShoppingListViewModel = koinInject()
    
    var itemToDelete by remember { mutableStateOf<ShoppingItemUi?>(null) }
    var itemToEdit by remember { mutableStateOf<ShoppingItemUi?>(null) }
    
    // ViewModelからデータを取得
    val items by viewModel.currentListItems.collectAsState()
    val shopsFromViewModel by viewModel.shops.collectAsState()
    
    // shopIdが指定されている場合、そのお店の商品のみを表示
    val filteredItems = remember(items, shopId) {
        if (shopId != null) {
            items.filter { it.shopId == shopId }
        } else {
            items
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<ItemCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (shopId != null) {
                                val shopName = shopsFromViewModel.firstOrNull { it.id == shopId }?.name ?: "お店"
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
            // カテゴリフィルタ
            CategoryFilterBar(
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { selectedCategoryFilter = it },
                items = filteredItems
            )
            
            // アイテムリスト
            ShoppingListContent(
                items = if (selectedCategoryFilter != null) {
                    filteredItems.filter { it.category == selectedCategoryFilter }
                } else {
                    filteredItems
                },
                showShopName = shopId == null, // 全体表示の時のみお店名を表示
                onToggleItem = { id ->
                    viewModel.toggleItemComplete(id)
                },
                onEditItem = { id ->
                    itemToEdit = filteredItems.firstOrNull { it.id == id }
                },
                onDeleteItem = { id ->
                    itemToDelete = filteredItems.firstOrNull { it.id == id }
                }
            )

            // アイテム追加ダイアログ
            AddItemDialog(
                isVisible = showAddDialog,
                currentShopId = shopId,
                availableShops = emptyList(), // お店固定なので選択不可
                onDismiss = { showAddDialog = false },
                onConfirm = { name, _, priority, category ->
                    viewModel.addItem(
                        name = name,
                        shopId = shopId, // 現在のお店のIDを使用
                        priority = priority,
                        category = category
                    )
                    showAddDialog = false
                    println("新しいアイテムが追加されました: $name")
                }
            )

            // アイテム編集ダイアログ
            EditItemDialog(
                item = itemToEdit,
                availableShops = emptyList(), // お店固定なので選択不可
                onDismiss = { itemToEdit = null },
                onConfirm = { name, _, priority ->
                    itemToEdit?.let { item ->
                        viewModel.updateItem(
                            itemId = item.id,
                            name = name,
                            shopId = item.shopId, // 既存のお店IDを保持
                            priority = priority,
                            category = item.category // 既存のカテゴリを保持
                        )
                    }
                    itemToEdit = null
                    println("アイテムが更新されました")
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
}

@Composable
private fun ShoppingListContent(
    items: List<ShoppingItemUi>,
    showShopName: Boolean = true,
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
                    onDelete = { onDeleteItem(item.id) },
                    showShopName = showShopName
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

@Composable
private fun CategoryFilterBar(
    selectedCategory: ItemCategory?,
    onCategorySelected: (ItemCategory?) -> Unit,
    items: List<ShoppingItemUi>,
    modifier: Modifier = Modifier
) {
    // アイテムに存在するカテゴリのみ表示
    val availableCategories = items.map { it.category }.distinct().sorted()
    
    if (availableCategories.isNotEmpty()) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 全て表示オプション
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("全て") }
                )
            }
            
            // 各カテゴリ
            items(availableCategories) { category ->
                val itemCount = items.count { it.category == category && !it.isCompleted }
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { 
                        Text("${category.displayName} ($itemCount)")
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = category.color,
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }
    }
}