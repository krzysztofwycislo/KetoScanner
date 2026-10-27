package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.RatingIcons
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.RatingIcon(rating: Rating, contentDescription: UiText, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors.result
    val (icon, tint) = when (rating) {
        Rating.Positive -> RatingIcons.Happy to colors.positive
        Rating.Neutral -> RatingIcons.Neutral to colors.neutral
        Rating.Negative -> RatingIcons.Sad to colors.negative
        Rating.Unknown -> RatingIcons.Question to colors.negative
    }
    Icon(
        imageVector = icon,
        contentDescription = contentDescription.resolve(),
        modifier = modifier.size(AppTheme.sizes.iconS),
        tint = tint,
    )
}

@Preview(showBackground = true)
@Composable
private fun RatingIconPreview() {
    AppTheme {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Rating.entries.forEach {
                Ui.RatingIcon(it, UiText.Res(R.string.designsystem_preview_rating))
            }
        }
    }
}
