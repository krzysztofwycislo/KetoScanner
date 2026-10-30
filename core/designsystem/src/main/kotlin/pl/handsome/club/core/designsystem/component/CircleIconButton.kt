package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.CircleIconButton(
    icon: ImageVector,
    contentDescription: UiText,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = AppTheme.colors.text.default,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(AppTheme.sizes.touchTarget),
        enabled = enabled,
        shape = AppTheme.shapes.circle,
        color = AppTheme.colors.background.surface,
        contentColor = contentColor,
        border = BorderStroke(AppTheme.borders.hairline, AppTheme.colors.border.default),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription.resolve(),
                modifier = Modifier.size(AppTheme.sizes.iconS),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CircleIconButtonPreview() {
    AppTheme {
        Ui.CircleIconButton(
            icon = AppIcons.Back.imageVector,
            contentDescription = UiText.Res(R.string.designsystem_back),
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
