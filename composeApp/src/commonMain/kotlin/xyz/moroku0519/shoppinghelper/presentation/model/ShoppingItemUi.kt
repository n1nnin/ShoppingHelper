package xyz.moroku0519.shoppinghelper.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import shoppinghelper.composeapp.generated.resources.Res
import shoppinghelper.composeapp.generated.resources.priority_high
import shoppinghelper.composeapp.generated.resources.priority_low
import shoppinghelper.composeapp.generated.resources.priority_normal
import shoppinghelper.composeapp.generated.resources.priority_urgent
import xyz.moroku0519.shoppinghelper.domain.model.Priority

data class ShoppingItemUi(
    val id: String,
    val name: String,
    val isCompleted: Boolean,
    val shopName: String?,
    val shopId: String?,
    val priority: Priority
) {
    val priorityColor: Color = priority.color
}

@Composable
fun Priority.getDisplayName(): String {
    return when (this) {
        Priority.LOW -> stringResource(Res.string.priority_low)
        Priority.NORMAL -> stringResource(Res.string.priority_normal)
        Priority.HIGH -> stringResource(Res.string.priority_high)
        Priority.URGENT -> stringResource(Res.string.priority_urgent)
    }
}