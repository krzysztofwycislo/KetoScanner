package pl.morning.tower.karbo.feature.preferences.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.OnboardingRepository

val preferencesDataModule = module {
    singleOf(::DietPreferencesPersistenceImpl) bind DietPreferencesPersistence::class
    singleOf(::DietPreferencesRepositoryImpl) bind DietPreferencesRepository::class
    singleOf(::OnboardingRepositoryImpl) bind OnboardingRepository::class
}
