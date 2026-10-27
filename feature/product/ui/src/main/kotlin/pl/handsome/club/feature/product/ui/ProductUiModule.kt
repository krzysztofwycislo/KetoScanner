package pl.handsome.club.feature.product.ui

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.handsome.club.feature.product.ui.state.ProductStateFactory

val productUiModule = module {
    viewModelOf(::ProductViewModel)
    factoryOf(::ProductStateFactory)
    factoryOf(::ProductResources)
}
