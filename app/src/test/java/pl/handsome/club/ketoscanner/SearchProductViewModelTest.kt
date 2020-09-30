package pl.handsome.club.ketoscanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import pl.handsome.club.ketoscanner.repository.ProductsRepository
import pl.handsome.club.ketoscanner.data.basicProductExample
import pl.handsome.club.ketoscanner.util.observeOnce
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel


@ExperimentalCoroutinesApi
class SearchProductViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var productsRepository: ProductsRepository

    private lateinit var searchProductViewModel: SearchProductViewModel


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        Dispatchers.setMain(Dispatchers.Default)
        searchProductViewModel = SearchProductViewModel(productsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when searching for existing product by name then product should be available`() =
        runBlocking {
            val product = basicProductExample()
            `when`(productsRepository.searchProductByName(product.name)).thenReturn(product)

            searchProductViewModel.searchByName(product.name)

            searchProductViewModel.getSearchedProduct().observeOnce {
                assertEquals(product, it)
            }
        }

    @Test
    fun `when searching for existing product by barcode then product should be available`() =
        runBlocking {
            val product = basicProductExample()
            `when`(productsRepository.searchProductByBarcode(product.barcode)).thenReturn(product)

            searchProductViewModel.searchProductByBarcode(product.barcode)

            searchProductViewModel.getSearchedProduct().observeOnce {
                assertEquals(product, it)
            }
        }

}