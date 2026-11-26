package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.mapFailures
import app.carbpeek.core.common.outcomeOf
import app.carbpeek.feature.preferences.api.ProfileSettingsRepository
import app.carbpeek.feature.preferences.api.model.ProfileSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class ProfileSettingsRepositoryImpl(
    private val persistence: DietPreferencesPersistence,
    private val dispatchers: AppDispatchers,
) : ProfileSettingsRepository {

    override fun observeSettings(): Flow<ProfileSettings> =
        persistence.observe()
            .map { stored ->
                ProfileSettings(
                    dietPreferences = stored.toDomain(),
                    opensDetailsRightAway = stored.opensDetailsRightAway,
                )
            }
            .distinctUntilChanged()
            .mapFailures(mapError = { AppError.Storage })
            .flowOn(dispatchers.io)

    override suspend fun saveSettings(settings: ProfileSettings): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.save(settings.toStored())
            }
        }
}
