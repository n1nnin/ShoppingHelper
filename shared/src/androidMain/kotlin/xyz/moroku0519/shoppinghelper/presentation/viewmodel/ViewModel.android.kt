package xyz.moroku0519.shoppinghelper.presentation.viewmodel

import androidx.lifecycle.ViewModel as AndroidViewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope

actual open class ViewModel : AndroidViewModel() {
    actual val viewModelScope = androidViewModelScope
}