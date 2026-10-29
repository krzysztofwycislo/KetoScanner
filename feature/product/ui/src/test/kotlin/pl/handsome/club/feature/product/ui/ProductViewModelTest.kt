package pl.handsome.club.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.product.api.ProductRepository
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
    private val products = FakeProductRepository()
    private val favourites = FakeFavourites()
    private val history = FakeHistory()
    private val logger = FakeAppLogger()
    private val errorReporter = FakeErrorReporter()
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
        analyzeProduct.clearProduct()
        products.gate()
        val viewModel = createViewModel()

        assertEquals(ProductContentState.Loading, viewModel.state.value.content)
    }

    @Test
    fun `when created with a route then it analyses the route barcode`() = runTest {
        createViewModel(route = ProductRoute(OTHER_BARCODE))

        assertEquals(setOf(OTHER_BARCODE), analyzeProduct.requestedBarcodes.toSet())
    }

    @Test
    fun `when created and a product is emitted then product is shown history saved and favourite status loaded`() =
        runTest {
            favourites.findResult = Outcome.Success(favouriteProduct)
            val viewModel = createViewModel()

            val content = showProduct(viewModel)
            assertEquals(setOf(BARCODE), analyzeProduct.requestedBarcodes.toSet())
            assertEquals(listOf(BARCODE), products.refreshedBarcodes)
            assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
            assertEquals(listOf(BARCODE), favourites.foundBarcodes)
            assertEquals(true, content.favourite?.isFavourite)
            assertTrue(logger.entries.isEmpty())
        }

    @Test
    fun `when created then product is refreshed once and history and favourites run once without any action`() =
        runTest {
            val gate = products.gate()
            val viewModel = createViewModel()

            gate.complete(Unit)

            assertEquals(1, products.refreshedBarcodes.size)
            assertEquals(1, history.saved.size)
            assertEquals(1, favourites.foundBarcodes.size)
            showProduct(viewModel)
    }

    @Test
    fun `when refresh fails with network before any product then error is shown and only a warning is logged`() =
        runTest {
            analyzeProduct.clearProduct()
            products.result = Outcome.Failure(AppError.Network)
            val viewModel = createViewModel()

            assertEquals(
                ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_network)),
                viewModel.state.value.content,
            )
            assertEquals(1, logger.entries.size)
            assertEquals(FakeAppLogger.Level.Warn, logger.entries.single().level)
            assertTrue(errorReporter.reports.isEmpty())
            assertTrue(history.saved.isEmpty())
        }

    @Test
    fun `when refresh fails with parsing before any product then error is shown and reported once`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Parsing)
        val viewModel = createViewModel()

        assertIs<ProductContentState.Error>(viewModel.state.value.content)
        assertEquals(AppError.Parsing, errorReporter.reports.single().error)
        assertTrue(logger.entries.isEmpty())
    }

    @Test
    fun `when refresh fails with unknown error before any product then it is reported with its cause`() = runTest {
        val cause = IllegalStateException("boom")
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Unknown(cause))
        val viewModel = createViewModel()

        assertEquals(AppError.Unknown(cause), errorReporter.reports.single().error)
        assertTrue(logger.entries.isEmpty())
    }

    @Test
    fun `when retry is clicked after an error then it refreshes again and a following product is shown`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        val observations = analyzeProduct.requestedBarcodes.size

        products.result = Outcome.Success(Unit)
        viewModel.onAction(ProductAction.OnRetryClicked)
        analyzeProduct.updates.emit(analysis)

        showProduct(viewModel)
        assertEquals(2, products.refreshedBarcodes.size)
        assertEquals(observations, analyzeProduct.requestedBarcodes.size)
    }

    @Test
    fun `when retry is clicked twice while the retry refresh is pending then each click asks for a refresh`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()

        val gate = products.gate()
        viewModel.onAction(ProductAction.OnRetryClicked)
        viewModel.onAction(ProductAction.OnRetryClicked)
        gate.complete(Unit)

        assertEquals(3, products.refreshedBarcodes.size)
    }

    @Test
    fun `when refresh fails with parsing while a product is shown then state stays without message and it is reported`() =
        runTest {
            products.result = Outcome.Failure(AppError.Parsing)
            val viewModel = createViewModel()
            collectMessages()

            showProduct(viewModel)
            assertTrue(messages.isEmpty())
            assertEquals(AppError.Parsing, errorReporter.reports.single().error)
            assertEquals("Refreshing product failed", errorReporter.reports.single().message)
            assertTrue(logger.entries.isEmpty())
        }

    @Test
    fun `when refresh fails with network while a product is shown then state stays and only a warning is logged`() =
        runTest {
            products.result = Outcome.Failure(AppError.Network)
            val viewModel = createViewModel()
            collectMessages()

            showProduct(viewModel)
            assertTrue(messages.isEmpty())
            assertTrue(errorReporter.reports.isEmpty())
            assertEquals(FakeAppLogger.Level.Warn, logger.entries.single().level)
        }

    @Test
    fun `when a product is emitted after the refresh failed then it is shown and the error is cleared`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        assertIs<ProductContentState.Error>(viewModel.state.value.content)

        analyzeProduct.updates.emit(analysis)

        showProduct(viewModel)
        assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
    }

    @Test
    fun `when a changed product is emitted later then it is shown and history and favourites run once`() = runTest {
        val viewModel = createViewModel()

        val changed = analysis.copy(product = product.copy(imageUrl = CHANGED_IMAGE_URL))
        analyzeProduct.updates.emit(changed)

        assertEquals(CHANGED_IMAGE_URL, showProduct(viewModel).header.imageUrl)
        assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
        assertEquals(listOf(BARCODE), favourites.foundBarcodes)
    }

    @Test
    fun `when observing fails before any product then storage error is shown reported and retry observes again`() =
        runTest {
            analyzeProduct.failure = IllegalStateException("disk")
            val viewModel = createViewModel()

            assertEquals(
                ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_storage)),
                viewModel.state.value.content,
            )
            assertEquals(AppError.Storage, errorReporter.reports.single().error)
            assertTrue(history.saved.isEmpty())
            val observations = analyzeProduct.requestedBarcodes.size

            analyzeProduct.failure = null
            viewModel.onAction(ProductAction.OnRetryClicked)

            showProduct(viewModel)
            assertTrue(analyzeProduct.requestedBarcodes.size > observations)
            assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
            assertEquals(listOf(BARCODE), favourites.foundBarcodes)
        }

    @Test
    fun `when created then the analysis is subscribed once and history and favourites run once`() = runTest {
        val viewModel = createViewModel()

        showProduct(viewModel)
        assertEquals(listOf(BARCODE), analyzeProduct.requestedBarcodes)
        assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
        assertEquals(listOf(BARCODE), favourites.foundBarcodes)
    }

    @Test
    fun `when observing fails and retry is clicked then history and favourites run once from the recovered observation`() =
        runTest {
            analyzeProduct.failure = IllegalStateException("disk")
            val viewModel = createViewModel()
            assertEquals(1, analyzeProduct.requestedBarcodes.size)

            analyzeProduct.failure = null
            viewModel.onAction(ProductAction.OnRetryClicked)

            showProduct(viewModel)
            assertEquals(2, analyzeProduct.requestedBarcodes.size)
            assertEquals(listOf<ProductAnalysis>(analysis), history.saved)
            assertEquals(listOf(BARCODE), favourites.foundBarcodes)
        }

    @Test
    fun `when observing fails after a product is shown then the product stays and it is reported`() = runTest {
        val viewModel = createViewModel()
        collectMessages()

        analyzeProduct.failure = IllegalStateException("disk")
        analyzeProduct.updates.emit(analysis)

        showProduct(viewModel)
        assertTrue(messages.isEmpty())
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
    }

    @Test
    fun `when the screen is closed while history is saving then the save still completes`() = runTest {
        val pending = CompletableDeferred<Unit>()
        history.pending = pending
        val viewModel = createViewModel()

        viewModel.viewModelScope.cancel()
        pending.complete(Unit)

        assertEquals(listOf<ProductAnalysis>(analysis), history.completed)
    }

    @Test
    fun `when history save fails then product is still shown and failure is only logged`() = runTest {
        history.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        collectMessages()

        showProduct(viewModel)
        assertEquals(1, logger.entries.size)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `when favourite lookup fails then there is no favourite button and failure is logged`() = runTest {
        favourites.findResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        assertNull(showProduct(viewModel).favourite)
        assertEquals(1, logger.entries.size)
    }

    @Test
    fun `when favourite is clicked for a non favourite then product is added and message is sent`() = runTest {
        val viewModel = createViewModel()
        collectMessages()

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

        viewModel.onAction(ProductAction.OnImageClicked)
        assertNotNull(showProduct(viewModel).imagePreview)
        assertEquals(true, handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))

        viewModel.onAction(ProductAction.OnImagePreviewDismissed)
        assertNull(showProduct(viewModel).imagePreview)
        assertEquals(false, handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))
    }

    @Test
    fun `when image is clicked for a product without image then preview stays closed`() = runTest {
        analyzeProduct.updates.emit(analysis.copy(product = product.copy(imageUrl = null)))
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnImageClicked)

        assertNull(showProduct(viewModel).imagePreview)
        assertNull(handle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN))
    }

    @Test
    fun `when handle was restored with an open preview then preview is shown after load`() = runTest {
        val viewModel = createViewModel(handle(KEY_IMAGE_PREVIEW_OPEN to true))

        assertNotNull(showProduct(viewModel).imagePreview)
    }

    @Test
    fun `when name is clicked then it toggles and the handle is written`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

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

        assertEquals(10, showProduct(viewModel).header.nameMaxLines)
    }

    private fun TestScope.createViewModel(
        handle: SavedStateHandle = handle(),
        route: ProductRoute = ProductRoute(BARCODE),
    ): ProductViewModel {
        val resources = ProductResources()
        return ProductViewModel(
            route = route,
            savedStateHandle = handle,
            analyzeProduct = analyzeProduct,
            products = products,
            favourites = favourites,
            history = history,
            appEvents = appEvents(),
            stateFactory = ProductStateFactory(resources),
            resources = resources,
            logger = logger,
            errorReporter = errorReporter,
            applicationScope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
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

        val updates = MutableSharedFlow<ProductAnalysis>(replay = 1).apply { tryEmit(analysis) }
        var failure: Throwable? = null
        val requestedBarcodes = mutableListOf<String>()

        fun clearProduct() = updates.resetReplayCache()

        override fun invoke(barcode: String): Flow<ProductAnalysis> = flow {
            requestedBarcodes += barcode
            updates.collect { analysis ->
                failure?.let { throw it }
                emit(analysis)
            }
        }
    }

    private class FakeProductRepository : ProductRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        val refreshedBarcodes = mutableListOf<String>()
        private var gate: CompletableDeferred<Unit>? = null

        fun gate(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

        override fun observeProduct(barcode: String): Flow<Product> = throw NotImplementedError()

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> {
            refreshedBarcodes += barcode
            gate?.await()
            return result
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

        override fun observeFavourites(nameQuery: String): Flow<Outcome<List<FavouriteProduct>>> =
            flowOf(Outcome.Success(emptyList()))

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
        var pending: CompletableDeferred<Unit>? = null
        val saved = mutableListOf<ProductAnalysis>()
        val completed = mutableListOf<ProductAnalysis>()

        override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> {
            saved += analysis
            pending?.await()
            completed += analysis
            return result
        }

        override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> =
            Outcome.Success(emptyList())
    }

    private companion object {
        const val BARCODE = "3017620422003"
        const val OTHER_BARCODE = "5900000000001"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"
        const val CHANGED_IMAGE_URL = "https://images.openfoodfacts.org/images/products/301/762/042/2003/back.jpg"

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
            macronutrients = MacronutrientAnalysisResult(DietMode.KETO, DietRate.NOT_ADVISED, emptyList(), 57.5, 69.6, 40),
        )

        val favouriteProduct = FavouriteProduct(
            productBarcode = BARCODE,
            productName = "Nutella",
            productBrand = "Ferrero",
            updateTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
            imageUrl = null,
        )

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf(*extra))
    }
}
