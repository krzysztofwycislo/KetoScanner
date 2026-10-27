package pl.handsome.club.feature.history.data

import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry

internal class AnalysisHistoryRepositoryImpl(
    private val persistence: AnalysisHistoryPersistence,
    private val dispatchers: AppDispatchers,
) : AnalysisHistoryRepository {

    override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.insert(analysis.toEntity())
            }
        }

    override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.lastUniqueEntries(amount).toDomain()
            }
        }
}
