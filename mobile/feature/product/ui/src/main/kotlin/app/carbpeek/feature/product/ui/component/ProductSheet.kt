package app.carbpeek.feature.product.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import app.carbpeek.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

private const val MIN_HEIGHT_FRACTION = 0.4f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductSheet(
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    content: @Composable (close: () -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val screenHeight = LocalWindowInfo.current.containerDpSize.height
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        containerColor = AppTheme.colors.background.surface,
        contentColor = AppTheme.colors.text.default,
        scrimColor = AppTheme.colors.background.scrim,
        dragHandle = null,
        contentWindowInsets = { WindowInsets() },
    ) {
        BackHandler(enabled = onBack != null) {
            onBack?.invoke()
        }
        Box(
            modifier = Modifier
                .heightIn(
                    min = screenHeight * MIN_HEIGHT_FRACTION,
                    max = screenHeight - AppTheme.sizes.sheetTopGap,
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                ),
            propagateMinConstraints = true,
        ) {
            content(close)
        }
    }
}
