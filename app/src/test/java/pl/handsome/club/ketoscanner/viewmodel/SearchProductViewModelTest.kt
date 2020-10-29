package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.domain.repository.ProductsRepository
import pl.handsome.club.ketoscanner.data.testProduct
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.product.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.product.SearchState


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchProductViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchProductViewModel

    @Mock
    private lateinit var productsRepository: ProductsRepository

    @Mock
    private lateinit var observer: Observer<SearchState>


    @Before
    fun init() {
        viewModel = SearchProductViewModel(productsRepository)
        viewModel.getSearchState().observeForever(observer)
    }

    @Test
    fun `when searching for existing product then success search state with given product be observer`() =
        coroutinesTestRule.runBlockingTest {
            val product = testProduct()
            `when`(productsRepository.searchProductByBarcode(product.barcode)).thenReturn(product)

            viewModel.searchProductByBarcode(product.barcode)

            verify(productsRepository).searchProductByBarcode(product.barcode)
            verify(observer).onChanged(SearchState.SearchingInProgress)
            verify(observer).onChanged(SearchState.SearchingSuccess(product))
        }

    @Test
    fun `when searching for product and exception occurred then error state should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val anyBarcode = anyString()
            val exampleException = IllegalStateException()
            `when`(productsRepository.searchProductByBarcode(anyBarcode)).thenThrow(exampleException)

            viewModel.searchProductByBarcode(anyBarcode)

            verify(productsRepository).searchProductByBarcode(anyBarcode)
            verify(observer).onChanged(SearchState.SearchingInProgress)
            verify(observer).onChanged(SearchState.SearchingError(exampleException))
        }

}