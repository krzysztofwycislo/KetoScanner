package pl.handsome.club.feature.preferences.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.coroutines.cancellation.CancellationException

internal class DietPreferencesRepositoryImpl(
    private val persistence: DietPreferencesPersistence,
    private val dispatchers: AppDispatchers,
) : DietPreferencesRepository {

    override fun observeDietPreferences(): Flow<Outcome<DietPreferences>> =
        persistence.observe()
            .map<StoredDietPreferences, Outcome<DietPreferences>> { stored -> Outcome.Success(stored.toDomain()) }
            .distinctUntilChanged()
            .catch { error ->
                if (error is CancellationException) throw error
                emit(Outcome.Failure(AppError.Storage))
            }
            .flowOn(dispatchers.io)

    override suspend fun getDietPreferences(): Outcome<DietPreferences> = observeDietPreferences().first()

    override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.save(preferences.maxDailyCarbsAmount, preferences.dietMode.name)
            }
        }

    private fun StoredDietPreferences.toDomain() = DietPreferences(
        maxDailyCarbsAmount = maxDailyCarbsAmount ?: DietPreferences.Default.maxDailyCarbsAmount,
        dietMode = DietMode.entries.firstOrNull { it.name == dietMode } ?: DietMode.KETO,
    )
}
