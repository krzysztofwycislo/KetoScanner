package app.carbpeek.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.carbpeek.navigation.presentation
import app.carbpeek.navigation.toBottomNavSection
import app.carbpeek.ui.AppAction
import app.carbpeek.ui.AppAction.OnDestinationChanged

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
