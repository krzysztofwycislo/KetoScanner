package pl.handsome.club.ketoscanner.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.ketoscanner.rule.CoroutineTestRule
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.analyze.ProductAnalysisState


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AnalyzeProductViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AnalyzeProductViewModel

    private val products = FakeProductRepository()

    @Mock
    private lateinit var observer: Observer<ProductAnalysisState>

    @Mock
    private lateinit var analyzeProduct: AnalyzeProduct


    @Before
    fun init() {
        viewModel = AnalyzeProductViewModel(products, analyzeProduct)
        viewModel.getProductAnalysisState().observeForever(observer)
    }

    @Test
    fun `when refresh succeeds then in progress and success with the requested barcode are observed`() =
        coroutinesTestRule.runTest {
            val analysis = ProductAnalysis.Rated(exampleProduct, exampleMacronutrientAnalysisResult)
            `when`(analyzeProduct.invoke(REQUESTED_BARCODE)).thenReturn(flowOf(analysis))

            viewModel.searchAndAnalyzeProduct(REQUESTED_BARCODE)

            verify(observer).onChanged(ProductAnalysisState.InProgress)
            verify(observer).onChanged(ProductAnalysisState.Success(REQUESTED_BARCODE, analysis))
        }

    @Test
    fun `when product is not found then product not found is observed and nothing is analysed`() =
        coroutinesTestRule.runTest {
            products.result = Outcome.Failure(AppError.NotFound)

            viewModel.searchAndAnalyzeProduct(REQUESTED_BARCODE)

            verify(observer).onChanged(ProductAnalysisState.ProductNotFound)
            verifyNoInteractions(analyzeProduct)
        }

    @Test
    fun `when network fails then error with the network failure is observed`() =
        coroutinesTestRule.runTest {
            products.result = Outcome.Failure(AppError.Network)

            viewModel.searchAndAnalyzeProduct(REQUESTED_BARCODE)

            verify(observer).onChanged(ProductAnalysisState.Error(AppError.Network))
        }

    private class FakeProductRepository : ProductRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)

        override fun observeProduct(barcode: String): Flow<Product> = emptyFlow()

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> = result
    }

    private companion object {
        const val REQUESTED_BARCODE = "0004963406"
    }

}
