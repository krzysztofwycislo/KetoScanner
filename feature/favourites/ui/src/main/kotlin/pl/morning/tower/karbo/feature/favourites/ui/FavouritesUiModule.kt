package pl.morning.tower.karbo.feature.favourites.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesStateFactory

val favouritesUiModule = module {
    viewModelOf(::FavouritesViewModel)
    factoryOf(::FavouritesStateFactory)
    factoryOf(::FavouritesResources)
}
