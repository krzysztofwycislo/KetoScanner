package pl.handsome.club.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.testing.FakeAppLogger
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateBack
import pl.handsome.club.feature.product.ui.state.ProductContentState
import pl.handsome.club.feature.product.ui.state.ProductStateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import pl.handsome.club.core.designsystem.R as DesignR

class ProductViewModelTest {

    private val analyzeProduct = FakeAnalyzeProduct()
    private val favourites = FakeFavourites()
    private val history = FakeHistory()
    private val logger = FakeAppLogger()
    private val messages = mutableListOf<AppEvent>()
    private var testAppEvents: AppEvents? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when created then content is loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(ProductContentState.Loading, viewModel.state.value.content)
    }

    @Test
    fun `when started and analysis succeeds then product is shown history saved and favourite status loaded`() = runTest {
        favourites.findResult = Outcome.Success(favouriteProduct)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)

        val content = showProduct(viewModel)
        assertEquals(listOf(BARCODE), analyzeProduct.requestedBarcodes)
        assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
        assertEquals(listOf(BARCODE), favourites.foundBarcodes)
        assertEquals(true, content.favourite?.isFavourite)
        assertTrue(logger.entries.isEmpty())
    }

    @Test
    fun `when started again after success then product is not analyzed again`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)
        viewModel.onAction(ProductAction.OnStart)

        assertEquals(1, analyzeProduct.requestedBarcodes.size)
        assertEquals(1, history.saved.size)
    }

    @Test
    fun `when started while analysis is suspended then product is analyzed once`() = runTest {
        val pending = CompletableDeferred<Outcome<ProductAnalysis>>()
        analyzeProduct.pending = pending
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)
        viewModel.onAction(ProductAction.OnStart)
        pending.complete(Outcome.Success(analysis))

        assertEquals(1, analyzeProduct.requestedBarcodes.size)
        showProduct(viewModel)
    }

    @Test
    fun `when analysis fails then error is shown and logged once`() = runTest {
        analyzeProduct.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)

        assertEquals(
            ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_network)),
            viewModel.state.value.content,
        )
        assertEquals(1, logger.entries.size)
        assertEquals(FakeAppLogger.Level.Error, logger.entries.single().level)
        assertTrue(history.saved.isEmpty())
    }

    @Test
    fun `when analysis fails with unknown error then cause is logged`() = runTest {
        val cause = IllegalStateException("boom")
        analyzeProduct.result = Outcome.Failure(AppError.Unknown(cause))
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)

        assertEquals(cause, logger.entries.single().cause)
    }

    @Test
    fun `when retry succeeds after an error then product is shown`() = runTest {
        analyzeProduct.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        viewModel.onAction(ProductAction.OnStart)

        analyzeProduct.result = Outcome.Success(analysis)
        viewModel.onAction(ProductAction.OnRetryClicked)

        showProduct(viewModel)
        assertEquals(2, analyzeProduct.requestedBarcodes.size)
    }

    @Test
    fun `when history save fails then product is still shown and failure is only logged`() = runTest {
        history.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        collectMessages()

        viewModel.onAction(ProductAction.OnStart)

        showProduct(viewModel)
        assertEquals(1, logger.entries.size)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `when favourite lookup fails then there is no favourite button and failure is logged`() = runTest {
        favourites.findResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnStart)

        assertNull(showProduct(viewModel).favourite)
        assertEquals(1, logger.entries.size)
    }

    @Test
    fun `when favourite is clicked for a non favourite then product is added and message is sent`() = runTest {
        val viewModel = createViewModel()
        collectMessages()
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnFavouriteClicked)

        assertEquals(listOf(product), favourites.added)
        assertTrue(favourites.removed.isEmpty())
        val button = assertNotNull(showProduct(viewModel).favourite)
        assertTrue(button.isFavourite)
        assertTrue(button.isEnabled)
        assertEquals(listOf<AppEvent>(AppEvent.ShowMessage(UiText.Res(R.string.product_added_to_favourites))), messages)
    }

    @Test
    fun `when favourite is clicked for a favourite then product is removed and message is sent`() = runTest {
        favourites.findResult = Outcome.Success(favouriteProduct)
        val viewModel = createViewModel()
        collectMessages()
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnFavouriteClicked)

        assertEquals(listOf(product), favourites.removed)
        assertTrue(favourites.added.isEmpty())
        assertFalse(assertNotNull(showProduct(viewModel).favourite).isFavourite)
        assertEquals(
            listOf<AppEvent>(AppEvent.ShowMessage(UiText.Res(R.string.product_removed_from_favourites))),
            messages,
        )
    }

    @Test
    fun `when favourite toggle fails then state is unchanged error is shown logged and button enabled again`() =
        runTest {
            favourites.updateResult = Outcome.Failure(AppError.Storage)
            val viewModel = createViewModel()
            collectMessages()
            viewModel.onAction(ProductAction.OnStart)

            viewModel.onAction(ProductAction.OnFavouriteClicked)

            val button = assertNotNull(showProduct(viewModel).favourite)
            assertFalse(button.isFavourite)
            assertTrue(button.isEnabled)
            assertEquals(
                listOf<AppEvent>(AppEvent.ShowMessage(UiText.Res(DesignR.string.designsystem_error_storage))),
                messages,
            )
            assertEquals(1, logger.entries.size)
        }

    @Test
    fun `when favourite is clicked while an update runs then the click is ignored`() = runTest {
        val pending = CompletableDeferred<Outcome<Unit>>()
        favourites.pendingUpdate = pending
        val viewModel = createViewModel()
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnFavouriteClicked)
        assertFalse(assertNotNull(showProduct(viewModel).favourite).isEnabled)
        viewModel.onAction(ProductAction.OnFavouriteClicked)
        pending.complete(Outcome.Success(Unit))

        assertEquals(1, favourites.added.size)
        assertTrue(assertNotNull(showProduct(viewModel).favourite).isEnabled)
    }

    @Test
    fun `when back is clicked then navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnBackClicked)

        assertEquals(NavigateBack, viewModel.events.first())
    }

    @Test
    fun `when image is clicked and dismissed then preview state and handle follow`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnImageClicked)
        assertNotNull(showProduct(viewModel).imagePreview)
        assertEquals(true, handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))

        viewModel.onAction(ProductAction.OnImagePreviewDismissed)
        assertNull(showProduct(viewModel).imagePreview)
        assertEquals(false, handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))
    }

    @Test
    fun `when image is clicked for a product without image then preview stays closed`() = runTest {
        analyzeProduct.result = Outcome.Success(analysis.copy(product = product.copy(imageUrl = null)))
        val handle = handle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnImageClicked)

        assertNull(showProduct(viewModel).imagePreview)
        assertNull(handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))
    }

    @Test
    fun `when handle was restored with an open preview then preview is shown after load`() = runTest {
        val viewModel = createViewModel(handle(KEY_IMAGE_PREVIEW_OPEN to true))

        viewModel.onAction(ProductAction.OnStart)

        assertNotNull(showProduct(viewModel).imagePreview)
    }

    @Test
    fun `when name is clicked then it toggles and the handle is written`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ProductAction.OnStart)

        viewModel.onAction(ProductAction.OnNameClicked)
        assertEquals(10, showProduct(viewModel).header.nameMaxLines)
        assertEquals(true, handle.get<Boolean>(KEY_NAME_EXPANDED))

        viewModel.onAction(ProductAction.OnNameClicked)
        assertEquals(1, showProduct(viewModel).header.nameMaxLines)
        assertEquals(false, handle.get<Boolean>(KEY_NAME_EXPANDED))
    }

    @Test
    fun `when handle was restored with an expanded name then name is expanded after load`() = runTest {
        val viewModel = createViewModel(handle(KEY_NAME_EXPANDED to true))

        viewModel.onAction(ProductAction.OnStart)

        assertEquals(10, showProduct(viewModel).header.nameMaxLines)
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = handle()): ProductViewModel {
        val resources = ProductResources()
        return ProductViewModel(
            savedStateHandle = handle,
            analyzeProduct = analyzeProduct,
            favourites = favourites,
            history = history,
            appEvents = appEvents(),
            stateFactory = ProductStateFactory(resources),
            resources = resources,
            logger = logger,
        )
    }

    private fun TestScope.appEvents(): AppEvents =
        testAppEvents ?: AppEvents(backgroundScope).also { testAppEvents = it }

    private fun TestScope.collectMessages() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents().events.toList(messages) }
    }

    private fun showProduct(viewModel: ProductViewModel): ProductContentState.ShowProduct =
        assertIs<ProductContentState.ShowProduct>(viewModel.state.value.content)

    private class FakeAnalyzeProduct : AnalyzeProduct {

        var result: Outcome<ProductAnalysis> = Outcome.Success(analysis)
        var pending: CompletableDeferred<Outcome<ProductAnalysis>>? = null
        val requestedBarcodes = mutableListOf<String>()

        override suspend fun invoke(barcode: String): Outcome<ProductAnalysis> {
            requestedBarcodes += barcode
            return pending?.await() ?: result
        }
    }

    private class FakeFavourites : FavouriteProductsRepository {

        var findResult: Outcome<FavouriteProduct?> = Outcome.Success(null)
        var updateResult: Outcome<Unit> = Outcome.Success(Unit)
        var pendingUpdate: CompletableDeferred<Outcome<Unit>>? = null
        val foundBarcodes = mutableListOf<String>()
        val added = mutableListOf<Product>()
        val removed = mutableListOf<Product>()

        override suspend fun addToFavourites(product: Product): Outcome<Unit> {
            added += product
            return pendingUpdate?.await() ?: updateResult
        }

        override suspend fun search(name: String): Flow<List<FavouriteProduct>> = flowOf(emptyList())

        override suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?> {
            foundBarcodes += barcode
            return findResult
        }

        override suspend fun removeFromFavourites(product: Product): Outcome<Unit> {
            removed += product
            return pendingUpdate?.await() ?: updateResult
        }
    }

    private class FakeHistory : AnalysisHistoryRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        val saved = mutableListOf<ProductAnalysis>()

        override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> {
            saved += analysis
            return result
        }

        override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> =
            Outcome.Success(emptyList())
    }

    private companion object {
        const val BARCODE = "3017620422003"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"

        val product = Product(
            name = "Nutella",
            barcode = BARCODE,
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front.jpg",
            servingAmount = "15",
        )

        val analysis = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(DietRate.NOT_ADVISED, 40, 69.6, 40),
        )

        val favouriteProduct = FavouriteProduct(
            productBarcode = BARCODE,
            productName = "Nutella",
            productBrand = "Ferrero",
            updateTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
            imageUrl = null,
        )

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf("barcode" to BARCODE, *extra))
    }
}
