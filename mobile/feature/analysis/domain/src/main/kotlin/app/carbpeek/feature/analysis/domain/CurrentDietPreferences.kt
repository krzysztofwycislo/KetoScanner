package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.catchAppError
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEmpty

internal const val CURRENT_DIET_PREFERENCES_TAG = "CurrentDietPreferences"

internal fun DietPreferencesRepository.currentDietPreferences(errorReporter: ErrorReporter): Flow<DietPreferences> =
    observeDietPreferences()
        .catchAppError { error ->
            errorReporter.report(
                tag = CURRENT_DIET_PREFERENCES_TAG,
                message = "Loading diet preferences failed",
                error = error,
            )
        }
        .onEmpty { emit(DietPreferences.Default) }
