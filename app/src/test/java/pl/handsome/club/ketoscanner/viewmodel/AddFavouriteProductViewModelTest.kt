package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddFavouriteProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddToFavouritesState


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddFavouriteProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModelAdd: AddFavouriteProductViewModel

    @Mock
    private lateinit var favouriteProductsRepository: FavouriteProductsRepository

    @Mock
    private lateinit var observer: Observer<AddToFavouritesState>


    @Before
    fun initViewModel() {
        viewModelAdd = AddFavouriteProductViewModel(favouriteProductsRepository)
        viewModelAdd.getAddToFavouritesState().observeForever(observer)
    }

    @Test
    fun `when we want to save favourite product then we should get success result`() {
        val product = exampleProduct

        viewModelAdd.addToFavourites(product)

        verify(observer).onChanged(AddToFavouritesState.InProgress)
        verify(observer).onChanged(AddToFavouritesState.Success)
    }

    @Test
    fun `when we want to save favourite product and exception occurred while adding to repository then we should get error result with given exception`() =
        coroutineTestRule.runBlockingTest {
            val product = exampleProduct
            val throwable = IllegalStateException()
            `when`(favouriteProductsRepository.addToFavourites(product)).thenThrow(throwable)

            viewModelAdd.addToFavourites(product)

            verify(observer).onChanged(AddToFavouritesState.InProgress)
            verify(observer).onChanged(AddToFavouritesState.Error(throwable))
        }

}