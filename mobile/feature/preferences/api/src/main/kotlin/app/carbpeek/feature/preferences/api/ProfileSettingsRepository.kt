package app.carbpeek.feature.preferences.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.preferences.api.model.ProfileSettings
import kotlinx.coroutines.flow.Flow

interface ProfileSettingsRepository {

    fun observeSettings(): Flow<ProfileSettings>

    suspend fun saveSettings(settings: ProfileSettings): Outcome<Unit>
}
