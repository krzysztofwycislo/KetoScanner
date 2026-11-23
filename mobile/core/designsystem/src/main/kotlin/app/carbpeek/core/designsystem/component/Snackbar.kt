package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme

private val SNACKBAR_ELEVATION = 8.dp

enum class MessageTone { Info, Success, Error }

class AppSnackbarVisuals(
    override val message: String,
    val tone: MessageTone,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Long
}

suspend fun SnackbarHostState.showAppMessage(message: String, tone: MessageTone) {
    showSnackbar(AppSnackbarVisuals(message, tone))
}

@Composable
fun Ui.Snackbar(
    message: String,
    tone: MessageTone,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val shape = AppTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.m)
            .shadow(SNACKBAR_ELEVATION, shape)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .clickable(onClick = onClick)
            .padding(AppTheme.spacing.s)
            .testTag("appSnackbar"),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.control.s)
                .background(tone.color(), AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tone.icon.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.inverse,
                modifier = Modifier.size(AppTheme.sizes.icon.m),
            )
        }
        Text(
            text = message,
            style = AppTheme.typography.body,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.text.default,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun Ui.SnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        Ui.Snackbar(
            message = data.visuals.message,
            tone = (data.visuals as? AppSnackbarVisuals)?.tone ?: MessageTone.Info,
            onClick = data::dismiss,
        )
    }
}

@Composable
private fun MessageTone.color(): Color = when (this) {
    MessageTone.Info -> AppTheme.colors.text.subtle
    MessageTone.Success -> AppTheme.colors.accent
    MessageTone.Error -> AppTheme.colors.verdict.negative.content
}

private val MessageTone.icon: AppIcon
    get() = when (this) {
        MessageTone.Info -> AppIcons.Info
        MessageTone.Success -> AppIcons.Check
        MessageTone.Error -> AppIcons.Cross
    }

@Preview(showBackground = true, backgroundColor = 0xFFF6F7F4)
@Composable
private fun SnackbarTonesPreview() {
    AppTheme {
        Column {
            Ui.Snackbar(
                message = stringResource(R.string.designsystem_preview_snackbar),
                tone = MessageTone.Info,
            )
            Ui.Snackbar(
                message = stringResource(R.string.designsystem_preview_snackbar_success),
                tone = MessageTone.Success,
            )
            Ui.Snackbar(
                message = stringResource(R.string.designsystem_preview_snackbar_error),
                tone = MessageTone.Error,
            )
        }
    }
}

@Preview
@Composable
private fun SnackbarOnCameraPreview() {
    AppTheme {
        Box(Modifier.background(AppTheme.colors.camera.background)) {
            Ui.Snackbar(
                message = stringResource(R.string.designsystem_preview_snackbar_error),
                tone = MessageTone.Error,
            )
        }
    }
}
