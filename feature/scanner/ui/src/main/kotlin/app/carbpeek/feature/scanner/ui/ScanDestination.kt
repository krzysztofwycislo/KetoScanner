package app.carbpeek.feature.scanner.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.scanner.ui.event.ScanEvent.ScanNavigation
import kotlinx.serialization.Serializable

@Serializable
data object ScanRoute

fun NavGraphBuilder.scanDestination(onNavigation: (ScanNavigation) -> Unit) {
    composable<ScanRoute> {
        ScanScreen(onNavigation)
    }
}
