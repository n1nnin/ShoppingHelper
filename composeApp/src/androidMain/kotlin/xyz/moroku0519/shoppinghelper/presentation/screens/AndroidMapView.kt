package xyz.moroku0519.shoppinghelper.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import xyz.moroku0519.shoppinghelper.model.Location
import xyz.moroku0519.shoppinghelper.model.ShopCategory
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

@Composable
actual fun AndroidMapView(
    shops: List<ShopUi>,
    currentLocation: Location,
    onShopClick: (ShopUi) -> Unit,
    modifier: Modifier
) {
    var isMapLoaded by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(currentLocation.latitude, currentLocation.longitude),
            12f
        )
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            ),
            onMapLoaded = {
                isMapLoaded = true
            }
        ) {
            // 現在地マーカー
            Marker(
                state = MarkerState(
                    position = LatLng(currentLocation.latitude, currentLocation.longitude)
                ),
                title = "現在地",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )

            // お店マーカー
            shops.forEach { shop ->
                val shopLocation = LatLng(
                    shop.latitude ?: 0.0,
                    shop.longitude ?: 0.0
                )

                val markerColor = when (shop.category) {
                    ShopCategory.GROCERY -> BitmapDescriptorFactory.HUE_GREEN
                    ShopCategory.PHARMACY -> BitmapDescriptorFactory.HUE_VIOLET
                    ShopCategory.CONVENIENCE -> BitmapDescriptorFactory.HUE_CYAN
                    else -> BitmapDescriptorFactory.HUE_RED
                }

                Marker(
                    state = MarkerState(position = shopLocation),
                    title = shop.name,
                    snippet = if (shop.pendingItemsCount > 0)
                        "${shop.pendingItemsCount}件の未購入アイテム"
                    else "買い物完了",
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                    onClick = {
                        onShopClick(shop)
                        true
                    }
                )
            }
        }

        // 読み込み中インジケーター
        if (!isMapLoaded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "地図を読み込み中...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}