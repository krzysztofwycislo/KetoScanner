package app.carbpeek.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.navigation.navigateToTab
import app.carbpeek.ui.AppNavigation
import app.carbpeek.ui.AppNavigation.NavigateToTab
import app.carbpeek.ui.AppNavigation.PopToTabRoot
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AppNavigationEffect(navigationEvents: Flow<AppNavigation>, navController: NavHostController) {
    ObserveEvents(navigationEvents) { navigation ->
        when (navigation) {
            is NavigateToTab -> navController.navigateToTab(navigation.tab)
            is PopToTabRoot -> navController.popBackStack(navigation.tab.route, inclusive = false)
        }
    }
}
