package pl.morning.tower.karbo.feature.history.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.history.ui.state.HistoryStateFactory

val historyUiModule = module {
    viewModelOf(::HistoryViewModel)
    factoryOf(::HistoryStateFactory)
    factoryOf(::HistoryResources)
}
