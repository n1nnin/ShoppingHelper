package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.di.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@Composable
fun AddShopDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, category: ShopCategory) -> Unit
) {
    if (isVisible) {
        var shopName by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(ShopCategory.GROCERY) }
        var showError by remember { mutableStateOf(false) }

        // ダイアログが開くたびに状態をリセット
        LaunchedEffect(isVisible) {
            if (isVisible) {
                shopName = ""
                address = ""
                selectedCategory = ShopCategory.GROCERY
                showError = false
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("お店を追加")
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
                        isError = showError && shopName.isBlank(),
                        supportingText = if (showError && shopName.isBlank()) {
                            { Text("お店名は必須です", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    // 住所入力（オプション）
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("住所（オプション）") },
                        placeholder = { Text("例: 東京都渋谷区神南1-1-1") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // カテゴリ選択
                    CategorySelector(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (shopName.isNotBlank()) {
                            onConfirm(
                                shopName.trim(),
                                address.trim(),
                                selectedCategory
                            )
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text("追加")
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
private fun CategorySelector(
    selectedCategory: ShopCategory,
    onCategorySelected: (ShopCategory) -> Unit
) {
    Column {
        Text(
            text = "カテゴリ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // カテゴリを2列のグリッドで表示
        val categories = ShopCategory.entries
        val chunkedCategories = categories.chunked(2)

        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 最後の行で要素が1つの場合、空のスペースを追加
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CategoryItem(
    category: ShopCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))

        // カテゴリカラーインジケーター
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = category.color,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = category.getDisplayName(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun AddShopDialogPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AddShopDialog(
                isVisible = true,
                onDismiss = {},
                onConfirm = { _, _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun CategorySelectorPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            CategorySelector(
                selectedCategory = ShopCategory.PHARMACY,
                onCategorySelected = {}
            )
        }
    }
}