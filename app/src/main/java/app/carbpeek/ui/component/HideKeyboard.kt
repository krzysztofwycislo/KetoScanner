package app.carbpeek.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
internal fun rememberHideKeyboard(): () -> Unit {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    return remember(keyboardController, focusManager) {
        {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
}
