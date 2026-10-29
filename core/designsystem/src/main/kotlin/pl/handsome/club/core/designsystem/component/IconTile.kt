package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.IconTile(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(AppTheme.sizes.iconTile)
            .background(AppTheme.colors.background.subtle, AppTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.text.subtle,
            modifier = Modifier.size(AppTheme.sizes.iconS),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconTilePreview() {
    AppTheme {
        Ui.IconTile(AppIcons.Scan, Modifier.padding(AppTheme.spacing.m))
    }
}
