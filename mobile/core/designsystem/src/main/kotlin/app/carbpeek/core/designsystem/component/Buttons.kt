package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: AppIcon? = null,
    loading: Boolean = false,
) {
    Button(
        onClick = {
            if (!loading) {
                onClick()
            }
        },
        modifier = modifier
            .heightIn(min = AppTheme.sizes.control.m)
            .semantics {
                if (loading) {
                    contentDescription = text
                }
            },
        enabled = enabled,
        shape = AppTheme.shapes.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
            contentColor = AppTheme.colors.text.inverse,
        ),
    ) {
        Ui.ContentCrossfade(targetState = loading, contentKey = { it }) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppTheme.sizes.icon.s),
                    color = LocalContentColor.current,
                    strokeWidth = AppTheme.borders.selected,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(AppTheme.sizes.icon.s),
                        )
                        Spacer(modifier = Modifier.width(AppTheme.spacing.xs))
                    }
                    Text(text = text, style = AppTheme.typography.button, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun Ui.SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = AppTheme.sizes.control.m),
        enabled = enabled,
        shape = AppTheme.shapes.control,
        border = BorderStroke(AppTheme.borders.control, AppTheme.colors.border.strong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppTheme.colors.background.surface,
            contentColor = AppTheme.colors.text.default,
        ),
    ) {
        Text(text = text, style = AppTheme.typography.button, textAlign = TextAlign.Center)
    }
}

@Composable
fun Ui.TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.control)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.button,
            color = AppTheme.colors.text.subtle,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    AppTheme {
        val text = stringResource(R.string.designsystem_preview_button)
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Ui.PrimaryButton(text = text, onClick = {})
            Ui.PrimaryButton(text = text, onClick = {}, enabled = false)
            Ui.PrimaryButton(text = text, onClick = {}, icon = AppIcons.Scan)
            Ui.PrimaryButton(text = text, onClick = {}, loading = true)
            Ui.SecondaryButton(text = text, onClick = {})
            Ui.SecondaryButton(text = text, onClick = {}, enabled = false)
            Ui.TextButton(text = text, onClick = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun ButtonsLargeFontPreview() {
    AppTheme {
        val text = stringResource(R.string.designsystem_preview_note)
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Ui.PrimaryButton(text = text, onClick = {})
            Ui.PrimaryButton(text = text, onClick = {}, icon = AppIcons.Scan)
            Ui.SecondaryButton(text = text, onClick = {})
            Ui.TextButton(text = text, onClick = {})
        }
    }
}
