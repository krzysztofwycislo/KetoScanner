package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Immutable
internal data class ScanStatePrototype(
    val query: String = "",
    val cameraPermission: CameraPermission = CameraPermission.Unchecked,
    val isScanPaused: Boolean = false,
    val isSearching: Boolean = false,
    val hasTorch: Boolean = false,
    val isTorchOn: Boolean = false,
    val isTypeSheetOpen: Boolean = false,
    val foundBarcode: String? = null,
    val foundAnalysis: ProductAnalysis? = null,
    val notFoundBarcode: String? = null,
    val tripEntries: List<ProductAnalysisHistoryEntry> = emptyList(),
    val now: Instant = Instant.fromEpochMilliseconds(0),
    val dietPreferences: DietPreferences? = null,
    val scanLock: Rect? = null,
) {

    val isScanBlocked: Boolean
        get() = isScanPaused || isSearching || isTypeSheetOpen || foundBarcode != null || notFoundBarcode != null

    companion object {
        val Default = ScanStatePrototype()
        const val TRIP_LIMIT = 3
        val TRIP_WINDOW = 3.hours
    }
}
