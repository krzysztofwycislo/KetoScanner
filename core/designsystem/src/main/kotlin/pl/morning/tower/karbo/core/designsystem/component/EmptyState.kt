package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme

@Composable
fun Ui.EmptyState(
    icon: AppIcon,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) AppTheme.sizes.iconDiscCompact else AppTheme.sizes.iconDisc)
                .background(AppTheme.colors.background.subtle, AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.muted,
                modifier = Modifier.size(if (compact) AppTheme.sizes.icon.m else AppTheme.sizes.icon.xl),
            )
        }
        Text(
            text = title,
            style = if (compact) AppTheme.typography.bodyStrong else AppTheme.typography.title,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = if (compact) AppTheme.typography.body else AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
        if (actionText != null) {
            Ui.PrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.padding(top = AppTheme.spacing.xs),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    AppTheme {
        Ui.EmptyState(
            icon = AppIcons.Clock,
            title = stringResource(R.string.designsystem_preview_title),
            body = stringResource(R.string.designsystem_preview_body),
            modifier = Modifier.padding(AppTheme.spacing.m),
            actionText = stringResource(R.string.designsystem_preview_button),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateCompactPreview() {
    AppTheme {
        Ui.EmptyState(
            icon = AppIcons.Scan,
            title = stringResource(R.string.designsystem_preview_title),
            body = stringResource(R.string.designsystem_preview_body),
            modifier = Modifier.padding(AppTheme.spacing.m),
            compact = true,
        )
    }
}
