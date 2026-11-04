package pl.morning.tower.karbo.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
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

    override fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>> =
        persistence.observeLastUniqueEntries(amount)
            .map<_, Outcome<List<ProductAnalysisHistoryEntry>>> { entities -> Outcome.Success(entities.toDomain()) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(Outcome.Failure(AppError.Storage))
            }
            .flowOn(dispatchers.io)
}
