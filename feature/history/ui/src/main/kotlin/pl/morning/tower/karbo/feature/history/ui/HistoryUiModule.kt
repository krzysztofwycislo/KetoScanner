package pl.handsome.club.feature.history.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.handsome.club.feature.history.ui.state.HistoryStateFactory

val historyUiModule = module {
    viewModelOf(::HistoryViewModel)
    factoryOf(::HistoryStateFactory)
    factoryOf(::HistoryResources)
}
