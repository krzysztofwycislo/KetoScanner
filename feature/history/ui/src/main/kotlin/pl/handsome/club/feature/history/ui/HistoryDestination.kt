package pl.handsome.club.feature.history.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation

@Serializable
data object HistoryRoute

fun NavGraphBuilder.historyDestination(onNavigation: (HistoryNavigation) -> Unit) {
    composable<HistoryRoute> { HistoryScreen(onNavigation) }
}
