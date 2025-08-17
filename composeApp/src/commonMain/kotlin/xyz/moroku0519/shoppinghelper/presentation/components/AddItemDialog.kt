package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.ItemCategory
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    isVisible: Boolean,
    currentShopId: String? = null,
    availableShops: List<ShopUi> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, shopId: String?, priority: Priority, category: ItemCategory) -> Unit
) {
    if (isVisible) {
        var itemName by remember { mutableStateOf("") }
        var selectedPriority by remember { mutableStateOf(Priority.NORMAL) }
        var selectedCategory by remember { mutableStateOf(ItemCategory.OTHER) }
        var selectedShopId by remember { mutableStateOf(currentShopId) }
        var showError by remember { mutableStateOf(false) }

        // ダイアログが開くたびに状態をリセット
        LaunchedEffect(isVisible) {
            if (isVisible) {
                itemName = ""
                selectedPriority = Priority.NORMAL
                selectedCategory = ItemCategory.OTHER
                selectedShopId = currentShopId
                showError = false
            }
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("新しいアイテムを追加") },
                            navigationIcon = {
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Default.Close, contentDescription = "閉じる")
                                }
                            },
                            actions = {
                                TextButton(
                                    onClick = {
                                        if (itemName.isNotBlank()) {
                                            onConfirm(
                                                itemName.trim(),
                                                selectedShopId,
                                                selectedPriority,
                                                selectedCategory
                                            )
                                            onDismiss()
                                        } else {
                                            showError = true
                                        }
                                    }
                                ) {
                                    Text("追加")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 商品名入力
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = {
                                itemName = it
                                showError = false
                            },
                            label = { Text("商品名") },
                            placeholder = { Text("例: 牛乳") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showError && itemName.isBlank(),
                            supportingText = if (showError && itemName.isBlank()) {
                                { Text("商品名は必須です", color = MaterialTheme.colorScheme.error) }
                            } else null
                        )

                        // お店選択（リスト管理から来た場合のみ表示）
                        if (currentShopId == null && availableShops.isNotEmpty()) {
                            ShopSelector(
                                selectedShopId = selectedShopId,
                                availableShops = availableShops,
                                onShopSelected = { selectedShopId = it }
                            )
                        }

                        // カテゴリ選択
                        CategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )

                        // 優先度選択
                        PrioritySelector(
                            selectedPriority = selectedPriority,
                            onPrioritySelected = { selectedPriority = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column {
        Text(
            text = "優先度",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Priority.entries.forEach { priority ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedPriority == priority),
                        onClick = { onPrioritySelected(priority) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedPriority == priority),
                    onClick = { onPrioritySelected(priority) }
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 優先度カラーインジケーター
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = priority.color,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = priority.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddItemDialogPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AddItemDialog(
                isVisible = true,
                currentShopId = "shop1",
                availableShops = emptyList(),
                onDismiss = {},
                onConfirm = { _, _, _, _ -> }
            )
        }
    }
}

@Composable
private fun ShopSelector(
    selectedShopId: String?,
    availableShops: List<ShopUi>,
    onShopSelected: (String?) -> Unit
) {
    Column {
        Text(
            text = "お店",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // "お店を選択しない"オプション
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (selectedShopId == null),
                    onClick = { onShopSelected(null) }
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (selectedShopId == null),
                onClick = { onShopSelected(null) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "お店を指定しない",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 利用可能なお店のリスト
        availableShops.forEach { shop ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedShopId == shop.id),
                        onClick = { onShopSelected(shop.id) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedShopId == shop.id),
                    onClick = { onShopSelected(shop.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))

                // お店のカテゴリカラーインジケーター
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = shop.category.color,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (shop.address.isNotEmpty()) {
                        Text(
                            text = shop.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: ItemCategory,
    onCategorySelected: (ItemCategory) -> Unit
) {
    Column {
        Text(
            text = "カテゴリ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 主要カテゴリを表示
        val mainCategories = listOf(
            ItemCategory.FOOD,
            ItemCategory.DAILY_GOODS,
            ItemCategory.MEDICINE,
            ItemCategory.CLOTHING,
            ItemCategory.OTHER
        )

        // グリッド形式でカテゴリを表示
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chunkedCategories = mainCategories.chunked(2)
            items(chunkedCategories) { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = category.color,
                                            shape = CircleShape
                                        )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 行に奇数個のアイテムがある場合の調整
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PrioritySelectorPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PrioritySelector(
                selectedPriority = Priority.HIGH,
                onPrioritySelected = {}
            )
        }
    }
}