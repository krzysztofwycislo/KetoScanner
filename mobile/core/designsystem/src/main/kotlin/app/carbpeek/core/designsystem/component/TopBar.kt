package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme

enum class TopBarNavigation { Back, Close }

@Composable
fun Ui.TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationIcon: TopBarNavigation = TopBarNavigation.Back,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = AppTheme.spacing.m,
                end = AppTheme.spacing.m,
                top = AppTheme.spacing.l,
                bottom = AppTheme.spacing.xs,
            ),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            val icon = when (navigationIcon) {
                TopBarNavigation.Back -> AppIcons.Back
                TopBarNavigation.Close -> AppIcons.Close
            }
            val description = when (navigationIcon) {
                TopBarNavigation.Back -> R.string.designsystem_back
                TopBarNavigation.Close -> R.string.designsystem_close
            }
            Ui.CircleIconButton(
                icon = icon.imageVector,
                contentDescription = UiText.Res(description),
                onClick = onBackClick,
            )
        }
        Text(
            text = title,
            style = AppTheme.typography.title,
            color = AppTheme.colors.text.default,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    AppTheme {
        Ui.TopBar(title = UiText.Res(R.string.designsystem_preview_title).resolve())
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarWithBackPreview() {
    AppTheme {
        Ui.TopBar(title = UiText.Res(R.string.designsystem_preview_title).resolve(), onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarWithClosePreview() {
    AppTheme {
        Ui.TopBar(
            title = UiText.Res(R.string.designsystem_preview_title).resolve(),
            onBackClick = {},
            navigationIcon = TopBarNavigation.Close,
        )
    }
}
