package pl.handsome.club.ketoscanner.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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

    private lateinit var viewModel: FavouriteProductsListViewModel


    private fun initializeViewModel() {
        viewModel = FavouriteProductsListViewModel(favouriteProductsRepository)
    }

    @Test
    fun loadFavouriteProductsTest_Success_WithProducts() {

    }

}