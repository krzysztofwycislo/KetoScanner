package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel
import java.util.*


@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(MockitoJUnitRunner::class)
class FavouriteProductsListViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var favouriteProductsRepository: FavouriteProductsRepository

    @Mock
    private lateinit var observer: Observer<List<FavouriteProduct>>

    private lateinit var viewModel: FavouriteProductsListViewModel


    private fun initViewModel(initialProductsList: List<FavouriteProduct>) = runBlockingTest {
        `when`(favouriteProductsRepository.search(anyString())).thenReturn(flow { emit(initialProductsList) })
        viewModel = FavouriteProductsListViewModel(favouriteProductsRepository)
        viewModel.favouriteProductsLiveData.observeForever(observer)
    }


    @Test
    fun `when we want to get initial favourite products list then we should get empty list`() = runBlockingTest {
        val emptyList = listOf<FavouriteProduct>()

        initViewModel(emptyList)

        verify(observer).onChanged(emptyList)
    }

    @Test
    fun `when we want to get initial favourite products list then we should get populated list`() = runBlockingTest {
        val product1 = FavouriteProduct("barcode", "testtest", "brand", Date(), null)
        val product2 = FavouriteProduct("barcode", "other", "brand", Date(), null)
        val allProducts = listOf(product1, product2)

        initViewModel(allProducts)

        verify(observer).onChanged(allProducts)
    }

    @Test
    fun `when we want to search products by name then we should get only products that contains given name`() = runBlockingTest {
        val testLikeNameProduct1 = FavouriteProduct("barcode", "test", "brand", Date(), null)
        val testLikeNameProduct2 = FavouriteProduct("barcode", "product test", "brand", Date(), null)
        val someOtherProduct = FavouriteProduct("barcode", "other", "brand", Date(), null)

        val allProducts = listOf(testLikeNameProduct1, someOtherProduct, testLikeNameProduct2)
        val testLikeNameProducts = listOf(testLikeNameProduct1, testLikeNameProduct2)

        initViewModel(allProducts)

        `when`(favouriteProductsRepository.search("test")).thenReturn(flow { emit(testLikeNameProducts) })
        viewModel.searchFavouriteProductsByName("test")

        verify(observer).onChanged(testLikeNameProducts)
    }

}