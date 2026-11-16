package app.carbpeek.feature.history.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.history.ui.event.HistoryEvent.HistoryNavigation
import kotlinx.serialization.Serializable

@Serializable
data object HistoryRoute

fun NavGraphBuilder.historyDestination(onNavigation: (HistoryNavigation) -> Unit) {
    composable<HistoryRoute> {
        HistoryScreen(onNavigation)
    }
}
