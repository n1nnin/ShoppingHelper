package xyz.moroku0519.shoppinghelper.presentation.components

import androidx.compose.runtime.Composable
import xyz.moroku0519.shoppinghelper.presentation.model.ShopUi

@Composable
expect fun GeofenceHandler(shops: List<ShopUi>)