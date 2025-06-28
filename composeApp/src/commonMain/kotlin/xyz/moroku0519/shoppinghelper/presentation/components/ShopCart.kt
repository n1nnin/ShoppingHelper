package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.getDisplayName

@Composable
fun ShopCard(
    shop: ShopUi,
    onShopClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onShopClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // カテゴリインジケーター
            Box(
                modifier = Modifier
                    .size(4.dp, 48.dp)
                    .background(
                        color = shop.categoryColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // カテゴリアイコン
            Icon(
                imageVector = getCategoryIcon(shop.category),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = shop.categoryColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // お店情報
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (shop.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shop.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                // ✅ String Resourceを使用
                Text(
                    text = shop.category.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = shop.categoryColor
                )
            }

            // アイテム数表示
            if (shop.totalItemsCount > 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (shop.pendingItemsCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("${shop.pendingItemsCount}")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${shop.totalItemsCount}件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // アクションボタン
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "編集",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// カテゴリごとのアイコン取得
private fun getCategoryIcon(category: ShopCategory) = when (category) {
    ShopCategory.GROCERY -> Icons.Default.ShoppingCart
    ShopCategory.PHARMACY -> Icons.Default.Phone
    ShopCategory.CONVENIENCE -> Icons.Default.AccountCircle
    ShopCategory.DEPARTMENT -> Icons.Default.Delete
    ShopCategory.ELECTRONICS -> Icons.Default.Search
    ShopCategory.CLOTHING -> Icons.Default.Close
    ShopCategory.RESTAURANT -> Icons.Default.Refresh
    ShopCategory.OTHER -> Icons.Default.Build
}

@Preview
@Composable
private fun ShopCardGroceryPreview() {
    MaterialTheme {
        ShopCard(
            shop = ShopUi(
                id = "preview1",
                name = "イオン渋谷店",
                address = "東京都渋谷区神南1-1-1 イオンビル1F",
                category = ShopCategory.GROCERY,
                pendingItemsCount = 3,
                totalItemsCount = 8
            ),
            onShopClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
private fun ShopCardPharmacyPreview() {
    MaterialTheme {
        ShopCard(
            shop = ShopUi(
                id = "preview2",
                name = "ツルハドラッグ",
                address = "東京都新宿区新宿3-1-1",
                category = ShopCategory.PHARMACY,
                pendingItemsCount = 0,
                totalItemsCount = 0
            ),
            onShopClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
private fun ShopCardLongTextPreview() {
    MaterialTheme {
        ShopCard(
            shop = ShopUi(
                id = "preview3",
                name = "とても長い名前のお店でテキストオーバーフローをテストします",
                address = "とても長い住所でテキストの省略表示をテストします。東京都渋谷区神南1-1-1 とても長いビル名 10階 1001号室",
                category = ShopCategory.DEPARTMENT,
                pendingItemsCount = 12,
                totalItemsCount = 25
            ),
            onShopClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
private fun ShopCardAllCategoriesPreview() {
    MaterialTheme {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(ShopCategory.values()) { category ->
                ShopCard(
                    shop = ShopUi(
                        id = "preview_${category.name}",
                        name = "${category.name}のお店",
                        address = "住所例",
                        category = category,
                        pendingItemsCount = if (category.ordinal % 2 == 0) category.ordinal else 0,
                        totalItemsCount = category.ordinal + 1
                    ),
                    onShopClick = {},
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShopCardEmptyPreview() {
    MaterialTheme {
        ShopCard(
            shop = ShopUi(
                id = "preview_empty",
                name = "新しいお店",
                address = "",
                category = ShopCategory.OTHER,
                pendingItemsCount = 0,
                totalItemsCount = 0
            ),
            onShopClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}