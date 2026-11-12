package pl.morning.tower.karbo.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.morning.tower.karbo.navigation.presentation
import pl.morning.tower.karbo.navigation.toBottomNavSection
import pl.morning.tower.karbo.ui.AppAction
import pl.morning.tower.karbo.ui.AppAction.OnDestinationChanged

@Composable
internal fun DestinationEffect(navController: NavHostController, onAction: (AppAction) -> Unit) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination
    val hideKeyboard = rememberHideKeyboard()

    LaunchedEffect(currentDestination?.id) { hideKeyboard() }
    LaunchedEffect(currentDestination?.id) {
        if (currentDestination != null) {
            onAction(
                OnDestinationChanged(
                    tab = currentDestination.toBottomNavSection(),
                    presentation = currentDestination.presentation(),
                ),
            )
        }
    }
}
