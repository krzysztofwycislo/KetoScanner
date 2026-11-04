package pl.handsome.club.feature.home.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.handsome.club.feature.home.ui.state.HomeStateFactory

val homeUiModule = module {
    viewModelOf(::HomeViewModel)
    factoryOf(::HomeStateFactory)
    factoryOf(::HomeResources)
}
