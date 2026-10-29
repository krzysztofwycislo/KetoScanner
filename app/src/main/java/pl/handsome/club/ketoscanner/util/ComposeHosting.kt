package pl.handsome.club.ketoscanner.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import pl.handsome.club.core.designsystem.theme.AppTheme

internal fun ComposeView.setHostedScreen(content: @Composable () -> Unit) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        AppTheme {
            // MainActivity already pads its root by the system bars, so the hosted screen must not apply them again (the IME inset stays).
            Box(
                Modifier
                    .consumeWindowInsets(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    .semantics { testTagsAsResourceId = true }
            ) {
                content()
            }
        }
    }
}
