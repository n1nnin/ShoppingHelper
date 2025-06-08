import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import xyz.moroku0519.shoppinghelper.presentation.navigation.ShoppingMemoNavigation
import xyz.moroku0519.shoppinghelper.presentation.screens.ShoppingListScreen

@Composable
fun App() {
    MaterialTheme {
        ShoppingMemoNavigation()
    }
}
