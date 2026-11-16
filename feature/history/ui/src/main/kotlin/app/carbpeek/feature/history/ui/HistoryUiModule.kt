package app.carbpeek.feature.history.ui

import app.carbpeek.feature.history.ui.state.HistoryStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyUiModule = module {
    viewModelOf(::HistoryViewModel)
    factoryOf(::HistoryStateFactory)
    factoryOf(::HistoryResources)
}
