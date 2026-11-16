package app.carbpeek.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
internal fun AppStartPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background.default),
    )
}
