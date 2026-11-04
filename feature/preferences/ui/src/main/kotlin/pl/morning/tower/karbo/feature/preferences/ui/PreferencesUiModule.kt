package pl.handsome.club.feature.preferences.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pl.handsome.club.core.common.APPLICATION_SCOPE
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory

val preferencesUiModule = module {
    viewModel {
        DietPreferencesViewModel(get(), get(), get(), get(), get(), get(), get(named(APPLICATION_SCOPE)))
    }
    factoryOf(::DietPreferencesStateFactory)
    factoryOf(::DietPreferencesResources)
}
