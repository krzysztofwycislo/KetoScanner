package app.carbpeek.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.carbpeek.feature.product.ui.state.ProductStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productUiModule = module {
    viewModel {
        ProductViewModel(
            get<SavedStateHandle>().toRoute<ProductRoute>(),
            get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    factoryOf(::ProductStateFactory)
    factoryOf(::ProductResources)
}
