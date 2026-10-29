package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Immutable
data class BottomBarItem(val label: String, val icon: Painter)

@Composable
fun Ui.BottomBar(
    items: ImmutableList<BottomBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = AppTheme.colors.background.default,
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemClick(index) },
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppTheme.sizes.iconS),
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomBarPreview() {
    AppTheme {
        Ui.BottomBar(
            items = persistentListOf(
                BottomBarItem(UiText.Res(R.string.designsystem_preview_title).resolve(), rememberVectorPainter(AppIcons.More)),
                BottomBarItem(UiText.Res(R.string.designsystem_preview_body).resolve(), rememberVectorPainter(AppIcons.Favourite)),
            ),
            selectedIndex = 0,
            onItemClick = {},
        )
    }
}
