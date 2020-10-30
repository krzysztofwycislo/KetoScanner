package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import pl.handsome.club.domain.product.SearchProduct


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
    private lateinit var observer: Observer<SearchProduct>


    @Before
    fun init() {
        viewModel = SearchProductViewModel(productsRepository)
        viewModel.getSearchState().observeForever(observer)
    }

    @Test
    fun `when searching for existing product then success search state with given product should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val product = testProduct()
            `when`(productsRepository.searchProductByBarcode(product.barcode))
                .thenReturn(SearchProduct.Success(product))

            viewModel.searchProductByBarcode(product.barcode)

            verify(productsRepository).searchProductByBarcode(product.barcode)
            verify(observer).onChanged(SearchProduct.InProgress)
            verify(observer).onChanged(SearchProduct.Success(product))
        }

    @Test
    fun `when searching for NON existing product then not found search state should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val barcode = anyString()
            `when`(productsRepository.searchProductByBarcode(barcode))
                .thenReturn(SearchProduct.NotFound)

            viewModel.searchProductByBarcode(barcode)

            verify(observer).onChanged(SearchProduct.InProgress)
            verify(observer).onChanged(SearchProduct.NotFound)
        }

    @Test
    fun `when searching for product and exception occurred then error state should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val anyBarcode = anyString()
            val exampleException = IllegalStateException()
            `when`(productsRepository.searchProductByBarcode(anyBarcode))
                .thenThrow(exampleException)

            viewModel.searchProductByBarcode(anyBarcode)

            verify(productsRepository).searchProductByBarcode(anyBarcode)
            verify(observer).onChanged(SearchProduct.InProgress)
            verify(observer).onChanged(SearchProduct.Error(exampleException))
        }

}