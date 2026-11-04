package pl.morning.tower.karbo.feature.about.ui

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutUiModule = module {
    viewModelOf(::AboutViewModel)
}
