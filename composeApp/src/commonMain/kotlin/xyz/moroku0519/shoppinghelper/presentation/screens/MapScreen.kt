package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import xyz.moroku0519.shoppinghelper.model.Location
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.components.LocationPermissionHandler
import xyz.moroku0519.shoppinghelper.presentation.components.LocationPermissionState
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi
import xyz.moroku0519.shoppinghelper.presentation.model.toUiModel
import kotlin.text.*

// ✅ expect宣言を追加
@Composable
expect fun AndroidMapView(
    shops: List<ShopUi>,
    currentLocation: Location,
    onShopClick: (ShopUi) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun rememberLocationPermissionState(): LocationPermissionState?

// 既存のMapScreen実装...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit
) {
    // 既存の実装はそのまま...
    val sampleShops = remember {
        listOf(
            Shop(
                id = "shop1",
                name = "イオン渋谷店",
                address = "東京都渋谷区神南1-1-1",
                category = ShopCategory.GROCERY,
                latitude = 35.6598, longitude = 139.7006
            ).toUiModel(pendingItemsCount = 3, totalItemsCount = 8),
            Shop(
                id = "shop2",
                name = "ツルハドラッグ新宿店",
                address = "東京都新宿区新宿3-1-1",
                category = ShopCategory.PHARMACY,
                latitude = 35.6896, longitude = 139.7006
            ).toUiModel(pendingItemsCount = 1, totalItemsCount = 2),
            Shop(
                id = "shop3",
                name = "セブンイレブン丸の内店",
                address = "東京都千代田区丸の内1-1-1",
                category = ShopCategory.CONVENIENCE,
                latitude = 35.6812, longitude = 139.7671
            ).toUiModel(pendingItemsCount = 0, totalItemsCount = 1)
        )
    }

    var currentLocation by remember { mutableStateOf(Location.TOKYO_STATION) }
    val locationPermissionState = rememberLocationPermissionState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("お店マップ")
                        Text(
                            text = "${sampleShops.size}件のお店",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    IconButton(
                        onClick = {
                            println("現在地ボタンがタップされました")
                        }
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "現在地"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LocationPermissionHandler(
            permissionState = locationPermissionState,
            onPermissionGranted = {
                MapContent(
                    shops = sampleShops,
                    currentLocation = currentLocation,
                    modifier = Modifier.padding(paddingValues)
                )
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun MapContent(
    shops: List<ShopUi>,
    currentLocation: Location,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        PreviewMapContent(shops, currentLocation, modifier)
    } else {
        RealMapContent(shops, currentLocation, modifier)
    }
}

@Composable
private fun RealMapContent(
    shops: List<ShopUi>,
    currentLocation: Location,
    modifier: Modifier = Modifier
) {
    // ✅ AndroidMapViewを使用
    AndroidMapView(
        shops = shops,
        currentLocation = currentLocation,
        onShopClick = { shop ->
            println("地図でお店クリック: ${shop.name}")
        },
        modifier = modifier
    )
}

@Composable
private fun PreviewMapContent(
    shops: List<ShopUi>,
    currentLocation: Location,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "地図機能（プレビュー）",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Android実機で実際の地図が表示されます",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "登録されているお店 (${shops.size}件)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    shops.take(3).forEach { shop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(shop.categoryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = shop.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (shop != shops.take(3).last()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}