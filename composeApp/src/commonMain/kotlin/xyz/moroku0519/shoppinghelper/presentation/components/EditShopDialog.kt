package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.model.Location
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel

@Composable
fun EditShopDialog(
    shop: ShopUi?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String?, category: ShopCategory) -> Unit
) {
    if (shop != null) {
        var shopName by remember(shop) { mutableStateOf(shop.name) }
        var address by remember(shop) { mutableStateOf(shop.address ?: "") }
        var selectedCategory by remember(shop) { mutableStateOf(shop.category) }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "お店を編集",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // お店名入力
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { 
                            shopName = it
                            showError = false
                        },
                        label = { Text("お店名") },
                        placeholder = { Text("例: イオン渋谷店") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("お店名を入力してください") }
                        } else null
                    )

                    // 住所入力（オプション）
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("住所（オプション）") },
                        placeholder = { Text("例: 東京都渋谷区...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // カテゴリ選択
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "カテゴリ",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ShopCategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }

                    // 現在のアイテム数情報（編集不可）
                    if (shop.totalItemsCount > 0) {
                        Text(
                            text = "登録アイテム: ${shop.totalItemsCount}件（未完了: ${shop.pendingItemsCount}件）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (shopName.isNotBlank()) {
                            onConfirm(
                                shopName.trim(),
                                address.trim().takeIf { it.isNotEmpty() },
                                selectedCategory
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
private fun ShopCategorySelector(
    selectedCategory: ShopCategory,
    onCategorySelected: (ShopCategory) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 上段：スーパー、薬局、コンビニ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ShopCategory.GROCERY,
                ShopCategory.PHARMACY,
                ShopCategory.CONVENIENCE
            ).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 下段：その他のカテゴリ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ShopCategory.BAKERY,
                ShopCategory.ELECTRONICS,
                ShopCategory.OTHER
            ).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun EditShopDialogPreview() {
    MaterialTheme {
        Surface {
            EditShopDialog(
                shop = Shop(
                    id = "shop1",
                    name = "イオン渋谷店",
                    address = "東京都渋谷区神南1-1-1",
                    location = Location(35.6598, 139.7006),
                    category = ShopCategory.GROCERY
                ).toUiModel(pendingItemsCount = 3, totalItemsCount = 8),
                onDismiss = {},
                onConfirm = { _, _, _ -> }
            )
        }
    }
}