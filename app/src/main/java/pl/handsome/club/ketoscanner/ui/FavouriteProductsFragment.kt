package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.FavoriteProductsFragmentBinding
import pl.handsome.club.ketoscanner.ui.adapter.FavouriteProductsListAdapter
import pl.handsome.club.ketoscanner.util.DebounceTextWatcher
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel


@ExperimentalCoroutinesApi
@FlowPreview
class FavouriteProductsFragment : Fragment(R.layout.favorite_products_fragment) {

    private val favouriteProductsListViewModel: FavouriteProductsListViewModel by viewModel()

    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private var _binding: FavoriteProductsFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FavoriteProductsFragmentBinding.bind(view)

        initializeFavouriteProductsList()

        initializeProductSearch()
    }

    private fun initializeProductSearch() {
        val textWatcher = DebounceTextWatcher(
            backgroundScope,
            favouriteProductsListViewModel::searchFavouriteProductsByName
        )
        binding.searchFavouriteProductEditText.addTextChangedListener(textWatcher)
    }

    private fun initializeFavouriteProductsList() {
        val adapter = FavouriteProductsListAdapter(::onProductClicked)

        favouriteProductsListViewModel.favouriteProductsLiveData
            .observe(viewLifecycleOwner, { adapter.setProducts(it) })

        binding.favouriteProductsList.adapter = adapter
    }

    private fun onProductClicked(favouriteProduct: FavouriteProduct) {
        FavouriteProductsFragmentDirections
            .toProductAnalysisResultFragment(favouriteProduct.productBarcode)
            .let(::safeNavigateTo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}