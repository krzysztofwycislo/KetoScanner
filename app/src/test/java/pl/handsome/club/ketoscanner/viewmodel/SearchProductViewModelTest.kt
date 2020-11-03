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
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.domain.product.ProductSearchState


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchProductViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchProductViewModel

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var observer: Observer<ProductSearchState>


    @Before
    fun init() {
        viewModel = SearchProductViewModel(productRepository)
        viewModel.getProductSearchState().observeForever(observer)
    }

    @Test
    fun `when searching for existing product then success search state with given product should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val product = testProduct
            `when`(productRepository.searchProductByBarcode(product.barcode))
                .thenReturn(ProductSearchState.Success(product))

            viewModel.searchProductByBarcode(product.barcode)

            verify(productRepository).searchProductByBarcode(product.barcode)
            verify(observer).onChanged(ProductSearchState.InProgress)
            verify(observer).onChanged(ProductSearchState.Success(product))
        }

    @Test
    fun `when searching for NON existing product then not found search state should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val barcode = anyString()
            `when`(productRepository.searchProductByBarcode(barcode))
                .thenReturn(ProductSearchState.NotFound)

            viewModel.searchProductByBarcode(barcode)

            verify(observer).onChanged(ProductSearchState.InProgress)
            verify(observer).onChanged(ProductSearchState.NotFound)
        }

    @Test
    fun `when searching for product and exception occurred then error state should be observer`() =
        coroutinesTestRule.runBlockingTest {
            val anyBarcode = anyString()
            val exampleException = IllegalStateException()
            `when`(productRepository.searchProductByBarcode(anyBarcode))
                .thenThrow(exampleException)

            viewModel.searchProductByBarcode(anyBarcode)

            verify(productRepository).searchProductByBarcode(anyBarcode)
            verify(observer).onChanged(ProductSearchState.InProgress)
            verify(observer).onChanged(ProductSearchState.Error(exampleException))
        }

}