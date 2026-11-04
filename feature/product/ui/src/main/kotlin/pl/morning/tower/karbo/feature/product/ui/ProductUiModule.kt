package pl.morning.tower.karbo.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.product.ui.state.ProductStateFactory

val productUiModule = module {
    viewModel {
        ProductViewModel(
            get<SavedStateHandle>().toRoute<ProductRoute>(),
            get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    factoryOf(::ProductStateFactory)
    factoryOf(::ProductResources)
}
