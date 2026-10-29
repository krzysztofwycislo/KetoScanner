package pl.handsome.club.feature.scanner.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation

@Serializable
data object ScanRoute

fun NavGraphBuilder.scanDestination(onNavigation: (ScanNavigation) -> Unit) {
    composable<ScanRoute> { ScanScreen(onNavigation) }
}
