package app.carbpeek.feature.preferences.data

import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.OnboardingRepository
import app.carbpeek.feature.preferences.api.ProfileSettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesDataModule = module {
    singleOf(::DietPreferencesPersistenceImpl) bind DietPreferencesPersistence::class
    singleOf(::DietPreferencesRepositoryImpl) bind DietPreferencesRepository::class
    singleOf(::ProfileSettingsRepositoryImpl) bind ProfileSettingsRepository::class
    singleOf(::OnboardingRepositoryImpl) bind OnboardingRepository::class
}
