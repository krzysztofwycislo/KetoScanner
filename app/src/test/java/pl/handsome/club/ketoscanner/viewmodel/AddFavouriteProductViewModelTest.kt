package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddFavouriteProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddToFavouritesState
import kotlin.time.Instant


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
        viewModelAdd.addToFavouritesState().observeForever(observer)
    }

    @Test
    fun `when we want to add favourite product then we should get success result`() {
        val product = exampleProduct

        viewModelAdd.addOrRemoveFromFavourites(product)

        verify(observer).onChanged(AddToFavouritesState.InProgress)
        verify(observer).onChanged(AddToFavouritesState.Success)
    }

    @Test
    fun `when we want to add favourite product that already is added then it should be removed`() = coroutineTestRule.runTest {
        val product = exampleProduct
        val favouriteProductInRepository = FavouriteProduct(product.barcode, product.name, product.brand, Instant.fromEpochMilliseconds(0), null)

        `when`(favouriteProductsRepository.findByBarcode(product.barcode)).thenReturn(favouriteProductInRepository)

        viewModelAdd.addOrRemoveFromFavourites(product)

        verify(observer).onChanged(AddToFavouritesState.InProgress)
        verify(observer).onChanged(AddToFavouritesState.Removed)
    }

    @Test
    fun `when we want to add favourite product and repository exception occurred then we should get error result with given exception`() =
        coroutineTestRule.runTest {
            val product = exampleProduct
            val throwable = IllegalStateException()
            `when`(favouriteProductsRepository.addToFavourites(product)).thenThrow(throwable)

            viewModelAdd.addOrRemoveFromFavourites(product)

            verify(observer).onChanged(AddToFavouritesState.InProgress)
            verify(observer).onChanged(AddToFavouritesState.Error(throwable))
        }

}