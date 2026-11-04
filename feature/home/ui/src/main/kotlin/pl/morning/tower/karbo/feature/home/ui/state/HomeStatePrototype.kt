package pl.handsome.club.feature.home.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.datetime.TimeZone
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.preferences.api.model.DietPreferences
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
