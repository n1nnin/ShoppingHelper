package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.Priority
import xyz.moroku0519.shoppinghelper.presentation.model.ShoppingItemUi
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@Composable
fun ShoppingItemCard(
    item: ShoppingItemUi,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showShopName: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // カテゴリ・優先度インジケーター
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // カテゴリインジケーター
                Box(
                    modifier = Modifier
                        .size(4.dp, 18.dp)
                        .background(
                            color = item.categoryColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                // 優先度インジケーター
                Box(
                    modifier = Modifier
                        .size(4.dp, 18.dp)
                        .background(
                            color = item.priorityColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // チェックボックス
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // アイテム情報
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isCompleted)
                        TextDecoration.LineThrough else null,
                    color = if (item.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                // お店名表示（全体リスト表示時のみ）
                if (showShopName) {
                    item.shopName?.let { shopName ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shopName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 優先度表示（String Resourceを使用）
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "優先度: ${item.priority.getDisplayName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = item.priorityColor
                )
            }

            // 削除ボタン
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShoppingItemCardNormalPreview() {
    MaterialTheme {
        ShoppingItemCard(
            item = ShoppingItemUi(
                id = "preview1",
                name = "牛乳",
                isCompleted = false,
                shopName = "スーパーマーケット",
                shopId = "shop1",
                priority = Priority.NORMAL
            ),
            onToggle = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun ShoppingItemCardCompletedPreview() {
    MaterialTheme {
        ShoppingItemCard(
            item = ShoppingItemUi(
                id = "preview2",
                name = "パン",
                isCompleted = true,
                shopName = "ベーカリー",
                shopId = "shop2",
                priority = Priority.HIGH
            ),
            onToggle = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun ShoppingItemCardUrgentPreview() {
    MaterialTheme {
        ShoppingItemCard(
            item = ShoppingItemUi(
                id = "preview3",
                name = "緊急で必要なもの",
                isCompleted = false,
                shopName = null,
                shopId = null,
                priority = Priority.URGENT
            ),
            onToggle = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun ShoppingItemCardAllPrioritiesPreview() {
    MaterialTheme {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(Priority.entries.toTypedArray()) { priority ->
                ShoppingItemCard(
                    item = ShoppingItemUi(
                        id = "preview_${priority.name}",
                        name = "${priority.name}のアイテム",
                        isCompleted = priority.ordinal % 2 == 0,
                        shopName = if (priority.ordinal % 3 == 0) null else "テストショップ",
                        shopId = "shop1",
                        priority = priority
                    ),
                    onToggle = {},
                    onEdit = {},
                    onDelete = {}
                )
            }
        }
    }
}