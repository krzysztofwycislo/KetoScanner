package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.Observer
import androidx.paging.PagedList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FavouriteProductsListViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @Mock
    private lateinit var favouriteProductsRepository: FavouriteProductsRepository

    @Mock
    private lateinit var observer: Observer<PagedList<FavouriteProduct>>

    private lateinit var viewModel: FavouriteProductsListViewModel


    private fun initializeViewModel() {
        viewModel = FavouriteProductsListViewModel(favouriteProductsRepository)
    }

    @Test
    fun loadFavouriteProductsTest_Success_WithProducts() {

    }

    @Test
    fun loadFavouriteProductsTest_Success_EmptyList() {

    }

    @Test
    fun loadFavouriteProductsTest_Success_NextPage_WithData() {

    }

    @Test
    fun loadFavouriteProductsTest_Success_NextPage_EmptyList() {

    }

    @Test
    fun loadFavouriteProductsTest_Failure() {

    }

}