package pl.handsome.club.ketoscanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.ketoscanner.data.basicProductExample
import pl.handsome.club.ketoscanner.repository.ProductsRepository
import pl.handsome.club.ketoscanner.rule.CoroutinesTestRule
import pl.handsome.club.ketoscanner.util.observeOnce
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchProductViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var productsRepository: ProductsRepository

    private lateinit var searchProductViewModel: SearchProductViewModel

    @Before
    fun init() {
        searchProductViewModel = SearchProductViewModel(productsRepository)
    }

    @Test
    fun `when searching for existing product by name then product should be available`() =
        coroutinesTestRule.runBlockingTest {
            val product = basicProductExample()
            `when`(productsRepository.searchProductByName(product.name)).thenReturn(product)

            searchProductViewModel.searchByName(product.name)

            verify(productsRepository).searchProductByName(product.name)
            searchProductViewModel.getSearchedProduct().observeOnce {
                assertEquals(product, it)
            }
        }

    @Test
    fun `when searching for existing product by barcode then product should be available`() =
        coroutinesTestRule.runBlockingTest {
            val product = basicProductExample()
            `when`(productsRepository.searchProductByBarcode(product.barcode)).thenReturn(product)

            searchProductViewModel.searchProductByBarcode(product.barcode)

            verify(productsRepository).searchProductByBarcode(product.barcode)
            searchProductViewModel.getSearchedProduct().observeOnce {
                assertEquals(product, it)
            }
        }

}