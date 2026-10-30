package pl.handsome.club.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import kotlin.coroutines.cancellation.CancellationException

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

    override fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>> =
        persistence.observeLastUniqueEntries(amount)
            .map<_, Outcome<List<ProductAnalysisHistoryEntry>>> { entities -> Outcome.Success(entities.toDomain()) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(Outcome.Failure(AppError.Storage))
            }
            .flowOn(dispatchers.io)
}
