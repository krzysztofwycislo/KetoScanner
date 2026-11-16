package app.carbpeek.feature.favourites.ui

import app.carbpeek.feature.favourites.ui.state.FavouritesStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val favouritesUiModule = module {
    viewModelOf(::FavouritesViewModel)
    factoryOf(::FavouritesStateFactory)
    factoryOf(::FavouritesResources)
}
