package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@Composable
fun EditItemDialog(
    item: ShoppingItemUi?,
    availableShops: List<ShopUi> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, shopId: String?, priority: Priority) -> Unit
) {
    if (item != null) {
        var itemName by remember(item) { mutableStateOf(item.name) }
        var selectedPriority by remember(item) { mutableStateOf(item.priority) }
        var selectedShopId by remember(item) { mutableStateOf(item.shopId) }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "アイテムを編集",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // アイテム名入力
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { 
                            itemName = it
                            showError = false
                        },
                        label = { Text("アイテム名") },
                        placeholder = { Text("例: 牛乳") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("アイテム名を入力してください") }
                        } else null
                    )

                    // お店選択（リスト管理から来た場合は変更可能）
                    if (availableShops.isNotEmpty()) {
                        ShopSelector(
                            selectedShopId = selectedShopId,
                            availableShops = availableShops,
                            onShopSelected = { selectedShopId = it }
                        )
                    } else if (item.shopName != null) {
                        CurrentShopInfo(shopName = item.shopName)
                    }

                    // 優先度選択
                    PrioritySelector(
                        selectedPriority = selectedPriority,
                        onPrioritySelected = { selectedPriority = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            onConfirm(
                                itemName.trim(),
                                selectedShopId,
                                selectedPriority
                            )
                            onDismiss()
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("更新")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
            }
        )
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
private fun CurrentShopInfo(shopName: String) {
    Column {
        Text(
            text = "所属するお店",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "（変更不可）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "優先度",
            style = MaterialTheme.typography.labelLarge
        )
        // 2行に分けて表示
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 最初の2つ（緊急、高）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Priority.URGENT, Priority.HIGH).forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { onPrioritySelected(priority) },
                        label = {
                            Text(
                                text = priority.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // 次の2つ（普通、低）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Priority.NORMAL, Priority.LOW).forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { onPrioritySelected(priority) },
                        label = {
                            Text(
                                text = priority.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditItemDialogPreview() {
    MaterialTheme {
        Surface {
            EditItemDialog(
                item = ShoppingItemUi(
                    id = "1",
                    name = "牛乳",
                    isCompleted = false,
                    shopName = "イオン",
                    shopId = "shop1",
                    priority = Priority.NORMAL
                ),
                onDismiss = {},
                onConfirm = { _, _, _ -> }
            )
        }
    }
}