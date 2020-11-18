package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import kotlinx.android.synthetic.main.favorite_products_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.ui.adapter.FavouriteProductsListAdapter
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel


class FavoriteProductsFragment : Fragment(R.layout.favorite_products_fragment) {

    private val favouriteProductsListViewModel: FavouriteProductsListViewModel by viewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFavouriteProductsList()
    }

    private fun initializeFavouriteProductsList() {
        val adapter = FavouriteProductsListAdapter(::onProductClicked)

        favouriteProductsListViewModel.getFavouriteProducts()
            .observe(viewLifecycleOwner, { adapter.submitList(it) })

        favouriteProductsList.adapter = adapter
    }

    private fun onProductClicked(favouriteProduct: FavouriteProduct) {
        val barcode = favouriteProduct.productBarcode

//        with(analyzeProductViewModel) {
//            searchAndAnalyzeProduct(barcode)
//            getProductAnalysisState().observe(viewLifecycleOwner, ::onProductSearchStateChanged)
//        }
    }

    private fun onFavouriteProductsChange(pagedList: PagedList<FavouriteProduct>) {

    }

}