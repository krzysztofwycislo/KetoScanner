package pl.morning.tower.karbo.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.navigation.navigateToTab
import pl.morning.tower.karbo.ui.AppNavigation
import pl.morning.tower.karbo.ui.AppNavigation.NavigateToTab
import pl.morning.tower.karbo.ui.AppNavigation.PopToTabRoot

@Composable
internal fun AppNavigationEffect(navigationEvents: Flow<AppNavigation>, navController: NavHostController) {
    ObserveEvents(navigationEvents) { navigation ->
        when (navigation) {
            is NavigateToTab -> navController.navigateToTab(navigation.tab)
            is PopToTabRoot -> navController.popBackStack(navigation.tab.route, inclusive = false)
        }
    }
}
