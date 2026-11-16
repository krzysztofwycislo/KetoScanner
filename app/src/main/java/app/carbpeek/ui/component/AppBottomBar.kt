package app.carbpeek.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.carbpeek.core.designsystem.component.AnimatedBottomBar
import app.carbpeek.core.designsystem.component.BottomBarItem
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.navigation.BottomNavSection
import app.carbpeek.ui.AppAction
import app.carbpeek.ui.AppAction.OnTabClicked
import app.carbpeek.ui.BottomBarState
import kotlinx.collections.immutable.toImmutableList

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
