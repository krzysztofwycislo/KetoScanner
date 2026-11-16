package app.carbpeek.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.OnboardingTip
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
    val pendingTips: Set<OnboardingTip> = emptySet(),
) {

    val isScanBlocked: Boolean
        get() = isScanPaused || isSearching || isTypeSheetOpen || foundBarcode != null || notFoundBarcode != null

    val isVerdictTipVisible: Boolean
        get() = OnboardingTip.VERDICT_TIP in pendingTips && foundAnalysis is ProductAnalysis.Rated

    companion object {
        val Default = ScanStatePrototype()
        const val TRIP_LIMIT = 3
        val TRIP_WINDOW = 3.hours
    }
}
