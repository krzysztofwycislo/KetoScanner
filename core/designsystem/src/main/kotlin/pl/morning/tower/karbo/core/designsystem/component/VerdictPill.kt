package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme

@Composable
fun Ui.VerdictPill(
    rating: Rating,
    text: UiText,
    modifier: Modifier = Modifier,
    contentDescription: UiText? = null,
) {
    val description = contentDescription?.resolve()
    val semantics = if (description != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = description }
    } else {
        Modifier
    }
    val color = rating.color()
    Row(
        modifier = modifier
            .then(semantics)
            .defaultMinSize(minHeight = AppTheme.sizes.control.xs)
            .background(rating.containerColor(), AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = rating.markIcon().imageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppTheme.sizes.icon.xs),
        )
        Text(
            text = text.resolve(),
            style = AppTheme.typography.label,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun Rating.markIcon() = when (this) {
    Rating.Positive -> AppIcons.Check
    Rating.Neutral -> AppIcons.Minus
    Rating.Negative -> AppIcons.Cross
    Rating.Unknown -> AppIcons.Question
}

@Preview(showBackground = true)
@Composable
private fun VerdictPillPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Rating.entries.forEach {
                Ui.VerdictPill(it, UiText.Res(R.string.designsystem_preview_rating))
            }
        }
    }
}
