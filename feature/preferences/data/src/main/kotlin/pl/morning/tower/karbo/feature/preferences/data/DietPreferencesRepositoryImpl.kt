package pl.morning.tower.karbo.feature.preferences.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.mapFailures
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences

internal class DietPreferencesRepositoryImpl(
    private val persistence: DietPreferencesPersistence,
    private val dispatchers: AppDispatchers,
) : DietPreferencesRepository {

    override fun observeDietPreferences(): Flow<DietPreferences> =
        persistence.observe()
            .map { stored -> stored.toDomain() }
            .distinctUntilChanged()
            .mapFailures(mapError = { AppError.Storage })
            .flowOn(dispatchers.io)

    override suspend fun getDietPreferences(): Outcome<DietPreferences> =
        outcomeOf(mapError = { AppError.Storage }) { observeDietPreferences().first() }

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
