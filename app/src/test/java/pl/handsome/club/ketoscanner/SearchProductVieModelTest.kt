package pl.handsome.club.ketoscanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
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
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel


class SearchProductVieModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var productsRepository : ProductsRepository

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
    fun searchProductByName() = runBlocking {
        // given existing product
        val product = basicProductExample()
        `when`(productsRepository.searchProductByName(product.name))
            .thenReturn(basicProductExample())

        // when we want to search our product by name
        searchProductViewModel.searchByName(product.name)

        // then searched product should appear TODO fixit
        searchProductViewModel.getSearchedProduct().observeForever {
            assertEquals(product, it)
        }
    }

    @Test
    fun searchProductByBarcode() = runBlocking {
        // given existing product
        val product = basicProductExample()
        `when`(productsRepository.searchProductByBarcode(product.barcode))
            .thenReturn(basicProductExample())

        // when we want to search our product by barcode
        searchProductViewModel.searchProductByBarcode(product.barcode)

        // then searched product should appear TODO fixit
        searchProductViewModel.getSearchedProduct().observeForever {
            assertEquals(product, it)
            assertEquals(product, it)
        }
    }

}