package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.mapFailures
import app.carbpeek.core.common.outcomeOf
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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
                persistence.save(
                    maxDailyCarbsAmount = preferences.maxDailyCarbsAmount,
                    dietMode = preferences.storedDietMode(),
                )
            }
        }

    private fun StoredDietPreferences.toDomain(): DietPreferences {
        val preset = DietPreset.of(
            DietPreferences(
                maxDailyCarbsAmount = maxDailyCarbsAmount ?: DietPreferences.Default.maxDailyCarbsAmount,
                dietMode = domainDietMode(),
            ),
        )
        return preset?.preferences ?: DietPreferences.NoDiet
    }

    private fun StoredDietPreferences.domainDietMode(): DietMode? =
        if (dietMode == NO_DIET_MODE) {
            null
        } else {
            DietMode.entries.firstOrNull { it.name == dietMode } ?: DietMode.KETO
        }
}
