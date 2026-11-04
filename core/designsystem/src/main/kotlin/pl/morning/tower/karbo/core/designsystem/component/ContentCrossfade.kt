package pl.handsome.club.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun <T : Any> Ui.ContentCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it::class },
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { (fadeIn() togetherWith fadeOut()).using(null) },
        label = "ContentCrossfade",
        contentKey = contentKey,
    ) { state -> content(state) }
}

@Preview(showBackground = true)
@Composable
private fun ContentCrossfadePreview() {
    AppTheme {
        Ui.ContentCrossfade(targetState = UiText.Res(R.string.designsystem_preview_body)) { text ->
            Text(
                text = text.resolve(),
                style = AppTheme.typography.body,
                modifier = Modifier.padding(AppTheme.spacing.m),
            )
        }
    }
}
