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
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AnalyzeProductViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AnalyzeProductViewModel

    @Mock
    private lateinit var observer: Observer<ProductAnalysisState>

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var preferencesRepository: DietPreferencesRepository

    @Mock
    private lateinit var dietAnalysisEngine: DietAnalysisEngine


    @Before
    fun init() {
        viewModel = AnalyzeProductViewModel(dietAnalysisEngine, preferencesRepository, productRepository)
        viewModel.getProductAnalysisState().observeForever(observer)
    }

    @Test
    fun `when we want to search and analyze product then result should be observed`() =
        coroutinesTestRule.runBlockingTest {
            val product = testProduct
            val preferences = exampleDietPreferences

            `when`(productRepository.searchProductByBarcode(product.barcode))
                .thenReturn(ProductSearchState.Success(product))

            `when`(preferencesRepository.getDietPreferences()).thenReturn(preferences)

            val resultToReturn = ProductAnalysisResult(product, null, null)
            `when`(dietAnalysisEngine.analyze(preferences, product)).thenReturn(resultToReturn)


            viewModel.searchAndAnalyzeProduct(product.barcode)


            verify(observer).onChanged(ProductAnalysisState.InProgress)
            verify(observer).onChanged(ProductAnalysisState.Success(resultToReturn))
        }

}