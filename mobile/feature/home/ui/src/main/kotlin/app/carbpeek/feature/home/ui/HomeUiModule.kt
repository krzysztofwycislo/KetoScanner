package app.carbpeek.feature.home.ui

import app.carbpeek.feature.home.ui.state.HomeStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeUiModule = module {
    viewModelOf(::HomeViewModel)
    factoryOf(::HomeStateFactory)
    factoryOf(::HomeResources)
}
