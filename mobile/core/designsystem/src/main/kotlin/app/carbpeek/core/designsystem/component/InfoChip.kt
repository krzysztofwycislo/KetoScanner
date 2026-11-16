package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.InfoChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val color = AppTheme.colors.text.subtle
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = AppTheme.sizes.control.xs)
            .background(AppTheme.colors.background.subtle, AppTheme.shapes.pill)
            .padding(start = AppTheme.spacing.xs, end = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
        Text(
            text = text,
            style = AppTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoChipPreview() {
    AppTheme {
        Ui.InfoChip(
            text = stringResource(R.string.designsystem_preview_title),
            icon = AppIcons.Cheese.imageVector,
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
