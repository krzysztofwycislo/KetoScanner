package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.ListRowCard(
    title: UiText,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: UiText? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = AppTheme.shapes.card
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.sizes.rowMinHeight)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppTheme.spacing.s, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.resolve(),
                style = AppTheme.typography.bodyStrong,
                color = AppTheme.colors.text.default,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle.resolve(),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun ListRowCardPreview() {
    AppTheme {
        Ui.ListRowCard(
            title = UiText.Res(R.string.designsystem_preview_title),
            subtitle = UiText.Res(R.string.designsystem_preview_body),
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
            leading = {
                Ui.IconTile(AppIcons.Scan.imageVector)
            },
            trailing = {
                Ui.VerdictPill(Rating.Positive, UiText.Res(R.string.designsystem_preview_rating))
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListRowCardTitleOnlyPreview() {
    AppTheme {
        Ui.ListRowCard(
            title = UiText.Res(R.string.designsystem_preview_title),
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
