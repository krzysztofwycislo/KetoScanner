package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.Chip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(AppTheme.sizes.button),
        shape = AppTheme.shapes.pill,
        color = AppTheme.colors.background.surface,
        contentColor = AppTheme.colors.text.default,
        border = BorderStroke(AppTheme.borders.hairline, AppTheme.colors.border.default),
    ) {
        Row(
            modifier = Modifier.padding(start = AppTheme.spacing.xs, end = AppTheme.spacing.s),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppTheme.sizes.chipIcon),
            )
            Text(
                text = text,
                style = AppTheme.typography.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipPreview() {
    AppTheme {
        Ui.Chip(
            text = stringResource(R.string.designsystem_preview_title),
            icon = AppIcons.Sliders,
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
