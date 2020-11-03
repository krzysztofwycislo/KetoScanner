package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository


object ViewModelFactory : ViewModelProvider.Factory {

    private val PRODUCT_REPOSITORY: ProductRepository =
        OpenFoodFactsRepository()


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            SearchProductViewModel::class.java -> SearchProductViewModel(PRODUCT_REPOSITORY)

            else -> modelClass.newInstance()
        }

        return viewModel as T
    }

}