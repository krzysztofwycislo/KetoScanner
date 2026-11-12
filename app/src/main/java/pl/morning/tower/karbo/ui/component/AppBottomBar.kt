package pl.morning.tower.karbo.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import pl.morning.tower.karbo.core.designsystem.component.AnimatedBottomBar
import pl.morning.tower.karbo.core.designsystem.component.BottomBarItem
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.navigation.BottomNavSection
import pl.morning.tower.karbo.ui.AppAction
import pl.morning.tower.karbo.ui.AppAction.OnTabClicked
import pl.morning.tower.karbo.ui.BottomBarState

@Composable
internal fun AppBottomBar(state: BottomBarState, onAction: (AppAction) -> Unit) {
    val items = BottomNavSection.entries
        .map { BottomBarItem(stringResource(it.label), it.icon, it.selectedIcon, it.isProminent) }
        .toImmutableList()

    Ui.AnimatedBottomBar(
        visible = state.isVisible,
        items = items,
        selectedIndex = state.selectedTab.ordinal,
        onItemClick = { index -> onAction(OnTabClicked(BottomNavSection.entries[index])) },
    )
}
