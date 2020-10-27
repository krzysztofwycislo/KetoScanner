package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.handsome.club.domain.repository.ProductsRepository
import pl.handsome.club.ketoscanner.viewmodel.product.SearchProductViewModel
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository


object ViewModelFactory : ViewModelProvider.Factory {

    private val productsRepository: ProductsRepository =
        OpenFoodFactsRepository()


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            SearchProductViewModel::class.java -> SearchProductViewModel(productsRepository)

            else -> modelClass.newInstance()
        }

        return viewModel as T
    }

}