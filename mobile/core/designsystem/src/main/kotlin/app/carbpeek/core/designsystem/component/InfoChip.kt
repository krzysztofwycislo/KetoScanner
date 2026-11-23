package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            .background(AppTheme.colors.background.subtle, AppTheme.shapes.extraLarge)
            .padding(
                start = AppTheme.spacing.xs,
                top = AppTheme.spacing.xxs,
                end = AppTheme.spacing.s,
                bottom = AppTheme.spacing.xxs,
            ),
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

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun InfoChipLargeFontPreview() {
    AppTheme {
        Ui.InfoChip(
            text = stringResource(R.string.designsystem_category_tea_and_greens),
            icon = AppIcons.Leaf.imageVector,
            modifier = Modifier
                .padding(AppTheme.spacing.m)
                .widthIn(max = AppTheme.sizes.control.xl * 3),
        )
    }
}
