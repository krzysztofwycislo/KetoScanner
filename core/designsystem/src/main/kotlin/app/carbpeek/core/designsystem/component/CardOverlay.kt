package app.carbpeek.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.core.designsystem.theme.spec

@Composable
fun <T : Any> Ui.CardOverlay(
    value: T?,
    onDismiss: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    scrimColor: Color = AppTheme.colors.camera.dim,
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        Scrim(
            visible = value != null,
            color = scrimColor,
            interaction = Modifier.pointerInput(Unit) {},
        )
        var lastValue by remember { mutableStateOf(value) }
        if (value != null) lastValue = value
        AnimatedVisibility(
            visible = value != null,
            enter = slideInVertically(AppTheme.motion.springSettle.spec(IntOffset.VisibilityThreshold)) { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
        ) {
            lastValue?.let {
                Ui.SwipeDownToDismiss(
                    visible = value != null,
                    onDismiss = onDismiss,
                ) {
                    content(it)
                }
            }
        }
    }
}

@Composable
fun Ui.SheetOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    scrimColor: Color = AppTheme.colors.camera.dim,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Scrim(
            visible = visible,
            color = scrimColor,
            interaction = Modifier.clickable(interactionSource = null, indication = null) { onDismiss() },
        )
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding())
                .imePadding(),
        ) {
            content()
        }
    }
}

@Composable
private fun BoxScope.Scrim(visible: Boolean, color: Color, interaction: Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.matchParentSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .then(interaction),
        )
    }
}

@Composable
private fun PreviewCard() {
    Text(
        text = UiText.Res(R.string.designsystem_preview_body).resolve(),
        style = AppTheme.typography.body,
        color = AppTheme.colors.text.default,
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.m)
            .background(AppTheme.colors.background.surface, AppTheme.shapes.extraLarge)
            .padding(AppTheme.spacing.l),
    )
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun CardOverlayPreview() {
    AppTheme {
        Ui.CardOverlay(
            value = Unit,
            onDismiss = {},
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxSize(),
        ) {
            PreviewCard()
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun SheetOverlayPreview() {
    AppTheme {
        Ui.SheetOverlay(
            visible = true,
            onDismiss = {},
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxSize(),
        ) {
            PreviewCard()
        }
    }
}
