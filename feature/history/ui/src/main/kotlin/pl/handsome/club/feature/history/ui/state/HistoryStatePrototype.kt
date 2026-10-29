package pl.handsome.club.feature.history.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry

@Immutable
internal data class HistoryStatePrototype(
    val entries: List<ProductAnalysisHistoryEntry>? = null,
    val isLoading: Boolean = false,
    val loadError: AppError? = null,
    val isMenuOpen: Boolean = false,
) {

    companion object {
        val Default = HistoryStatePrototype()
        const val ENTRIES_LIMIT = 50
    }
}
