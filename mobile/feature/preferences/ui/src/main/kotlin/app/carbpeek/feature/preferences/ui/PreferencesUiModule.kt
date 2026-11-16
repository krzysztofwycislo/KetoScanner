package app.carbpeek.feature.preferences.ui

import app.carbpeek.core.common.APPLICATION_SCOPE
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val preferencesUiModule = module {
    viewModel {
        DietPreferencesViewModel(get(), get(), get(), get(), get(), get(), get(named(APPLICATION_SCOPE)))
    }
    factoryOf(::DietPreferencesStateFactory)
    factoryOf(::DietPreferencesResources)
}
