package pl.handsome.club.feature.preferences.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository

val preferencesDataModule = module {
    singleOf(::DietPreferencesPersistenceImpl) bind DietPreferencesPersistence::class
    singleOf(::DietPreferencesRepositoryImpl) bind DietPreferencesRepository::class
}
