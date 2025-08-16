package xyz.moroku0519.shoppinghelper.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import shoppinghelper.composeapp.generated.resources.Res
import shoppinghelper.composeapp.generated.resources.*
import xyz.moroku0519.shoppinghelper.model.Shop
import xyz.moroku0519.shoppinghelper.model.ShopCategory

data class ShopUi(
    val id: String,
    val name: String,
    val address: String,
    val category: ShopCategory,
    val pendingItemsCount: Int = 0,
    val totalItemsCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val categoryColor: Color = category.color
}

@Composable
fun ShopCategory.getDisplayName(): String {
    return when (this) {
        ShopCategory.GROCERY -> stringResource(Res.string.shop_category_grocery)
        ShopCategory.PHARMACY -> stringResource(Res.string.shop_category_pharmacy)
        ShopCategory.CONVENIENCE -> stringResource(Res.string.shop_category_convenience)
        ShopCategory.BAKERY -> stringResource(Res.string.shop_category_bakery)
        ShopCategory.DEPARTMENT -> stringResource(Res.string.shop_category_department)
        ShopCategory.ELECTRONICS -> stringResource(Res.string.shop_category_electronics)
        ShopCategory.CLOTHING -> stringResource(Res.string.shop_category_clothing)
        ShopCategory.RESTAURANT -> stringResource(Res.string.shop_category_restaurant)
        ShopCategory.OTHER -> stringResource(Res.string.shop_category_other)
    }
}

fun Shop.toUiModel(pendingItemsCount: Int = 0, totalItemsCount: Int = 0): ShopUi {
    return ShopUi(
        id = id,
        name = name,
        address = address ?: "",
        category = category,
        pendingItemsCount = pendingItemsCount,
        totalItemsCount = totalItemsCount,
        latitude = latitude,
        longitude = longitude
    )
}
