package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.VerdictBanner(
    rating: Rating,
    title: UiText,
    details: List<UiText>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val color = rating.color()
    CompositionLocalProvider(LocalContentColor provides color) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(rating.containerColor(), AppTheme.shapes.large)
                .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(AppTheme.sizes.iconTile)
                        .background(color, AppTheme.shapes.circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = rating.markIcon(),
                        contentDescription = null,
                        tint = AppTheme.colors.text.inverse,
                        modifier = Modifier.size(AppTheme.sizes.iconS),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                    Text(text = title.resolve(), style = AppTheme.typography.headline, color = color)
                    details.forEach {
                        Text(text = it.resolve(), style = AppTheme.typography.bodyStrong, color = color)
                    }
                }
            }
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerdictBannerPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Rating.entries.forEach {
                Ui.VerdictBanner(
                    rating = it,
                    title = UiText.Res(R.string.designsystem_preview_rating),
                    details = listOf(UiText.Res(R.string.designsystem_preview_share, UiArg.Numeric.Percent(0.19))),
                ) {
                    Text(text = UiText.Res(R.string.designsystem_preview_body).resolve(), style = AppTheme.typography.body)
                }
            }
        }
    }
}
