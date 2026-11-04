package pl.handsome.club.feature.favourites.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.handsome.club.feature.favourites.ui.state.FavouritesStateFactory

val favouritesUiModule = module {
    viewModelOf(::FavouritesViewModel)
    factoryOf(::FavouritesStateFactory)
    factoryOf(::FavouritesResources)
}
