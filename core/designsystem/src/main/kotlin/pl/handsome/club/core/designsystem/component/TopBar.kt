package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
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
            Ui.CircleIconButton(
                icon = AppIcons.Back.imageVector,
                contentDescription = UiText.Res(R.string.designsystem_back),
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
