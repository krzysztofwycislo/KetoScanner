package app.carbpeek.feature.home.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.common.AppError
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Immutable
internal data class HomeStatePrototype(
    val entries: List<ProductAnalysisHistoryEntry>? = null,
    val loadError: AppError? = null,
    val dietPreferences: DietPreferences? = null,
    val now: Instant = Instant.fromEpochMilliseconds(0),
    val timeZone: TimeZone = TimeZone.UTC,
) {

    companion object {
        val Default = HomeStatePrototype()
        const val RECENT_LIMIT = 4
    }
}
