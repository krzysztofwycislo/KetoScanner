package app.carbpeek.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientName
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.favourites.api.FavouriteProductsRepository
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.ProductRepository
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateBack
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateToAddProduct
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateToReport
import app.carbpeek.feature.product.ui.state.IngredientSheetState
import app.carbpeek.feature.product.ui.state.ProductContentState
import app.carbpeek.feature.product.ui.state.ProductStateFactory
import app.carbpeek.feature.product.ui.state.VerdictState
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import app.carbpeek.core.designsystem.R as DesignR

class ProductViewModelTest {

    private val analyzeProduct = FakeAnalyzeProduct()
    private val products = FakeProductRepository()
    private val favourites = FakeFavourites()
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
    fun `when created and a product is emitted then product is shown and favourite status loaded`() =
        runTest {
            favourites.isFavouriteResult = Outcome.Success(true)
            val viewModel = createViewModel()

            val content = showProduct(viewModel)
            assertEquals(setOf(BARCODE), analyzeProduct.requestedBarcodes.toSet())
            assertEquals(listOf(BARCODE), products.refreshedBarcodes)
            assertEquals(listOf(BARCODE), favourites.foundBarcodes)
            assertEquals(true, content.favourite?.isFavourite)
        }

    @Test
    fun `when created then product is refreshed once and favourites run once without any action`() =
        runTest {
            val gate = products.gate()
            val viewModel = createViewModel()

            gate.complete(Unit)

            assertEquals(1, products.refreshedBarcodes.size)
            assertEquals(1, favourites.foundBarcodes.size)
            showProduct(viewModel)
    }

    @Test
    fun `when refresh fails with network before any product then error is shown and passed to the reporter`() =
        runTest {
            analyzeProduct.clearProduct()
            products.result = Outcome.Failure(AppError.Network)
            val viewModel = createViewModel()

            assertEquals(
                ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_network)),
                viewModel.state.value.content,
            )
            assertEquals(AppError.Network, errorReporter.reports.single().error)
        }

    @Test
    fun `when refresh fails with parsing before any product then error is shown and reported once`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Parsing)
        val viewModel = createViewModel()

        assertIs<ProductContentState.Error>(viewModel.state.value.content)
        assertEquals(AppError.Parsing, errorReporter.reports.single().error)
    }

    @Test
    fun `when refresh fails with unknown error before any product then it is reported with its cause`() = runTest {
        val cause = IllegalStateException("boom")
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Unknown(cause))
        val viewModel = createViewModel()

        assertEquals(AppError.Unknown(cause), errorReporter.reports.single().error)
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
    fun `when retry is clicked after a timeout then it refreshes again`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Timeout)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnRetryClicked)

        assertEquals(2, products.refreshedBarcodes.size)
        assertEquals(
            ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_timeout)),
            viewModel.state.value.content,
        )
    }

    @Test
    fun `when retry is clicked twice while the retry refresh is pending then each click asks for a refresh`() =
        runTest {
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
    fun `when refresh fails with parsing while a product is shown then state stays without message and is reported`() =
        runTest {
            products.result = Outcome.Failure(AppError.Parsing)
            val viewModel = createViewModel()
            collectMessages()

            showProduct(viewModel)
            assertTrue(messages.isEmpty())
            assertEquals(AppError.Parsing, errorReporter.reports.single().error)
            assertEquals("Refreshing product failed", errorReporter.reports.single().message)
        }

    @Test
    fun `when refresh fails with network while a product is shown then state stays and it is passed to the reporter`() =
        runTest {
            products.result = Outcome.Failure(AppError.Network)
            val viewModel = createViewModel()
            collectMessages()

            showProduct(viewModel)
            assertTrue(messages.isEmpty())
            assertEquals(AppError.Network, errorReporter.reports.single().error)
        }

    @Test
    fun `when a product is emitted after the refresh failed then it is shown and the error is cleared`() = runTest {
        analyzeProduct.clearProduct()
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        assertIs<ProductContentState.Error>(viewModel.state.value.content)

        analyzeProduct.updates.emit(analysis)

        showProduct(viewModel)
    }

    @Test
    fun `when a changed product is emitted later then it is shown and favourites run once`() = runTest {
        val viewModel = createViewModel()

        val changed = analysis.copy(product = product.copy(imageUrl = CHANGED_IMAGE_URL))
        analyzeProduct.updates.emit(changed)

        assertEquals(CHANGED_IMAGE_URL, showProduct(viewModel).header.imageUrl)
        assertEquals(listOf(BARCODE), favourites.foundBarcodes)
    }

    @Test
    fun `when the analysis is re-rated after a profile change then the new verdict is shown`() = runTest {
        val viewModel = createViewModel()
        assertEquals(Rating.Negative, assertIs<VerdictState.Rated>(showProduct(viewModel).verdict).rating)

        analyzeProduct.updates.emit(
            analysis.copy(
                macronutrients = analysis.macronutrients.copy(dietMode = DietMode.LOW_CARB, rate = DietRate.GOOD),
            ),
        )

        assertEquals(Rating.Positive, assertIs<VerdictState.Rated>(showProduct(viewModel).verdict).rating)
        assertEquals(1, analyzeProduct.requestedBarcodes.size)
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
            val observations = analyzeProduct.requestedBarcodes.size

            analyzeProduct.failure = null
            viewModel.onAction(ProductAction.OnRetryClicked)

            showProduct(viewModel)
            assertTrue(analyzeProduct.requestedBarcodes.size > observations)
            assertEquals(listOf(BARCODE), favourites.foundBarcodes)
        }

    @Test
    fun `when created then the analysis is subscribed once and favourites run once`() = runTest {
        val viewModel = createViewModel()

        showProduct(viewModel)
        assertEquals(listOf(BARCODE), analyzeProduct.requestedBarcodes)
        assertEquals(listOf(BARCODE), favourites.foundBarcodes)
    }

    @Test
    fun `when observing fails and retry is clicked then favourites run once from the recovered observation`() =
        runTest {
            analyzeProduct.failure = IllegalStateException("disk")
            val viewModel = createViewModel()
            assertEquals(1, analyzeProduct.requestedBarcodes.size)

            analyzeProduct.failure = null
            viewModel.onAction(ProductAction.OnRetryClicked)

            showProduct(viewModel)
            assertEquals(2, analyzeProduct.requestedBarcodes.size)
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
    fun `when favourite lookup fails then there is no favourite button and the failure is reported`() = runTest {
        favourites.isFavouriteResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        assertNull(showProduct(viewModel).favourite)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
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
        assertEquals(
            listOf<AppEvent>(
                AppEvent.ShowMessage(UiText.Res(R.string.product_added_to_favourites), MessageTone.Success),
            ),
            messages,
        )
    }

    @Test
    fun `when favourite is clicked for a favourite then product is removed and message is sent`() = runTest {
        favourites.isFavouriteResult = Outcome.Success(true)
        val viewModel = createViewModel()
        collectMessages()

        viewModel.onAction(ProductAction.OnFavouriteClicked)

        assertEquals(listOf(product), favourites.removed)
        assertTrue(favourites.added.isEmpty())
        assertFalse(assertNotNull(showProduct(viewModel).favourite).isFavourite)
        assertEquals(
            listOf<AppEvent>(
                AppEvent.ShowMessage(UiText.Res(R.string.product_removed_from_favourites), MessageTone.Success),
            ),
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
                listOf<AppEvent>(
                    AppEvent.ShowMessage(UiText.Res(DesignR.string.designsystem_error_storage), MessageTone.Error),
                ),
                messages,
            )
            assertEquals(AppError.Storage, errorReporter.reports.single().error)
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

    @Test
    fun `when basis is switched then macros selection and the handle follow`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnPerServingClicked)
        assertTrue(showProduct(viewModel).macros.isPerServingSelected)
        assertEquals(true, handle.get<Boolean>(KEY_PER_SERVING_SELECTED))

        viewModel.onAction(ProductAction.OnPer100gClicked)
        assertFalse(showProduct(viewModel).macros.isPerServingSelected)
        assertEquals(false, handle.get<Boolean>(KEY_PER_SERVING_SELECTED))
    }

    @Test
    fun `when handle was restored with per serving selected then per serving is shown after load`() = runTest {
        val viewModel = createViewModel(handle(KEY_PER_SERVING_SELECTED to true))

        assertTrue(showProduct(viewModel).macros.isPerServingSelected)
    }

    @Test
    fun `when an ingredient is clicked and the sheet dismissed then sheet state and handle follow`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnIngredientClicked(2))
        assertNotNull(showProduct(viewModel).ingredientSheet)
        assertEquals(2, handle.get<Int>(KEY_OPEN_INGREDIENT))

        viewModel.onAction(ProductAction.OnIngredientSheetDismissed)
        assertNull(showProduct(viewModel).ingredientSheet)
        assertNull(handle.get<Int>(KEY_OPEN_INGREDIENT))
    }

    @Test
    fun `when handle was restored with an open ingredient then the sheet is shown once the product is shown`() =
        runTest {
            analyzeProduct.clearProduct()
            products.gate()
            val viewModel = createViewModel(handle(KEY_OPEN_INGREDIENT to 1))
            assertEquals(ProductContentState.Loading, viewModel.state.value.content)

            analyzeProduct.updates.emit(analysisWithIngredients)

            assertNotNull(showProduct(viewModel).ingredientSheet)
        }

    @Test
    fun `when a content is clicked and left with back then the compound is shown and the handle follows`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnIngredientClicked(COMPOUND_INDEX))
        viewModel.onAction(ProductAction.OnIngredientContentClicked(1))
        assertEquals(UiText.Raw("Sugar"), ingredientSheet(viewModel).title)
        assertNotNull(ingredientSheet(viewModel).back)
        assertEquals(COMPOUND_INDEX, handle.get<Int>(KEY_OPEN_INGREDIENT))
        assertEquals(1, handle.get<Int>(KEY_OPEN_INGREDIENT_CONTENT))

        viewModel.onAction(ProductAction.OnIngredientBackClicked)
        assertEquals(UiText.Raw("Cocoa"), ingredientSheet(viewModel).title)
        assertNull(ingredientSheet(viewModel).back)
        assertEquals(COMPOUND_INDEX, handle.get<Int>(KEY_OPEN_INGREDIENT))
        assertNull(handle.get<Int>(KEY_OPEN_INGREDIENT_CONTENT))
    }

    @Test
    fun `when back is clicked twice then the compound stays open`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnIngredientClicked(COMPOUND_INDEX))
        viewModel.onAction(ProductAction.OnIngredientContentClicked(0))
        viewModel.onAction(ProductAction.OnIngredientBackClicked)
        viewModel.onAction(ProductAction.OnIngredientBackClicked)

        assertEquals(UiText.Raw("Cocoa"), ingredientSheet(viewModel).title)
    }

    @Test
    fun `when the sheet is dismissed with a content open then both are closed`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnIngredientClicked(COMPOUND_INDEX))
        viewModel.onAction(ProductAction.OnIngredientContentClicked(0))
        viewModel.onAction(ProductAction.OnIngredientSheetDismissed)

        assertNull(showProduct(viewModel).ingredientSheet)
        assertNull(handle.get<Int>(KEY_OPEN_INGREDIENT))
        assertNull(handle.get<Int>(KEY_OPEN_INGREDIENT_CONTENT))
    }

    @Test
    fun `when another ingredient is opened then no content is open`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ProductAction.OnIngredientClicked(COMPOUND_INDEX))
        viewModel.onAction(ProductAction.OnIngredientContentClicked(0))
        viewModel.onAction(ProductAction.OnIngredientClicked(2))
        assertEquals(UiText.Raw("Vanillin"), ingredientSheet(viewModel).title)
        assertNull(handle.get<Int>(KEY_OPEN_INGREDIENT_CONTENT))

        viewModel.onAction(ProductAction.OnIngredientClicked(COMPOUND_INDEX))
        assertEquals(UiText.Raw("Cocoa"), ingredientSheet(viewModel).title)
        assertNull(ingredientSheet(viewModel).back)
    }

    @Test
    fun `when handle was restored with an open content then the content is shown once the product is shown`() =
        runTest {
            analyzeProduct.clearProduct()
            products.gate()
            val viewModel = createViewModel(
                handle(KEY_OPEN_INGREDIENT to COMPOUND_INDEX, KEY_OPEN_INGREDIENT_CONTENT to 1),
            )
            assertEquals(ProductContentState.Loading, viewModel.state.value.content)

            analyzeProduct.updates.emit(analysisWithIngredients)

            assertEquals(UiText.Raw("Sugar"), ingredientSheet(viewModel).title)
            assertNotNull(ingredientSheet(viewModel).back)
        }

    @Test
    fun `when the ingredients info is clicked and dismissed then sheet state and handle follow`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val handle = handle()
        val viewModel = createViewModel(handle)
        assertNull(showProduct(viewModel).ingredientsInfoSheet)

        viewModel.onAction(ProductAction.OnIngredientsInfoClicked)
        assertNotNull(showProduct(viewModel).ingredientsInfoSheet)
        assertEquals(true, handle.get<Boolean>(KEY_INGREDIENTS_INFO_OPEN))

        viewModel.onAction(ProductAction.OnIngredientsInfoDismissed)
        assertNull(showProduct(viewModel).ingredientsInfoSheet)
        assertEquals(false, handle.get<Boolean>(KEY_INGREDIENTS_INFO_OPEN))
    }

    @Test
    fun `when the ingredients info is clicked twice then its sheet stays open`() = runTest {
        analyzeProduct.updates.emit(analysisWithIngredients)
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnIngredientsInfoClicked)
        viewModel.onAction(ProductAction.OnIngredientsInfoClicked)

        assertNotNull(showProduct(viewModel).ingredientsInfoSheet)
    }

    @Test
    fun `when handle was restored with the ingredients info open then its sheet is shown with the product`() =
        runTest {
            analyzeProduct.clearProduct()
            products.gate()
            val viewModel = createViewModel(handle(KEY_INGREDIENTS_INFO_OPEN to true))
            assertEquals(ProductContentState.Loading, viewModel.state.value.content)

            analyzeProduct.updates.emit(analysisWithIngredients)

            assertNotNull(showProduct(viewModel).ingredientsInfoSheet)
        }

    @Test
    fun `when add photos is clicked then navigates to add the product with the barcode`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnAddPhotosClicked)

        assertEquals(NavigateToAddProduct(BARCODE), viewModel.events.first())
    }

    @Test
    fun `when report clicked then navigates to report with the barcode`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ProductAction.OnReportClicked)

        assertEquals(NavigateToReport(BARCODE), viewModel.events.first())
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
            appEvents = appEvents(),
            stateFactory = ProductStateFactory(resources),
            resources = resources,
            errorReporter = errorReporter,
        )
    }

    private fun TestScope.appEvents(): AppEvents =
        testAppEvents ?: AppEvents(backgroundScope).also { testAppEvents = it }

    private fun TestScope.collectMessages() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents().events.toList(messages) }
    }

    private fun showProduct(viewModel: ProductViewModel): ProductContentState.ShowProduct =
        assertIs<ProductContentState.ShowProduct>(viewModel.state.value.content)

    private fun ingredientSheet(viewModel: ProductViewModel): IngredientSheetState =
        assertNotNull(showProduct(viewModel).ingredientSheet)

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

        override fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>> =
            throw NotImplementedError()

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> {
            refreshedBarcodes += barcode
            gate?.await()
            return result
        }
    }

    private class FakeFavourites : FavouriteProductsRepository {

        var isFavouriteResult: Outcome<Boolean> = Outcome.Success(false)
        var updateResult: Outcome<Unit> = Outcome.Success(Unit)
        var pendingUpdate: CompletableDeferred<Outcome<Unit>>? = null
        val foundBarcodes = mutableListOf<String>()
        val added = mutableListOf<Product>()
        val removed = mutableListOf<Product>()

        override suspend fun addToFavourites(product: Product): Outcome<Unit> {
            added += product
            return pendingUpdate?.await() ?: updateResult
        }

        override fun observeFavourites(nameQuery: String): Flow<List<FavouriteProduct>> =
            flowOf(emptyList())

        override suspend fun isFavourite(barcode: Barcode): Outcome<Boolean> {
            foundBarcodes += barcode.value
            return isFavouriteResult
        }

        override suspend fun removeFromFavourites(product: Product): Outcome<Unit> {
            removed += product
            return pendingUpdate?.await() ?: updateResult
        }
    }

    private companion object {
        const val BARCODE = "3017620422003"
        const val OTHER_BARCODE = "5900000000001"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"
        const val KEY_PER_SERVING_SELECTED = "perServingSelected"
        const val KEY_OPEN_INGREDIENT = "openIngredient"
        const val KEY_OPEN_INGREDIENT_CONTENT = "openIngredientContent"
        const val COMPOUND_INDEX = 1
        const val KEY_INGREDIENTS_INFO_OPEN = "ingredientsInfoOpen"
        const val CHANGED_IMAGE_URL = "https://images.openfoodfacts.org/images/products/301/762/042/2003/back.jpg"

        val product = Product(
            name = "Nutella",
            barcode = BARCODE,
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front.jpg",
            servingAmount = "15",
            category = ProductCategory.SPREADS,
        )

        val analysis = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                dietMode = DietMode.KETO,
                rate = DietRate.NOT_ADVISED,
                reasons = emptyList(),
                carbsPer100g = 57.5,
                dailyCarbConsumption = 40,
            ),
        )

        val analysisWithIngredients = analysis.copy(
            ingredients = IngredientsAnalysis(
                ingredients = listOf(
                    ingredient(name = "hazelnuts", id = "en:hazelnut"),
                    ingredient(
                        name = "cocoa",
                        id = "en:cocoa",
                        contains = listOf(
                            ingredient(name = "cocoa powder", id = "en:cocoa-powder"),
                            ingredient(name = "sugar", id = "en:sugar"),
                        ),
                    ),
                    ingredient(name = "vanillin", id = "en:vanillin"),
                ),
            ),
        )

        fun ingredient(
            name: String,
            id: String,
            contains: List<AnalyzedIngredient> = emptyList(),
        ) = AnalyzedIngredient(
            id = id,
            name = IngredientName(name = name, labelName = name, eNumber = null, vitamin = null),
            declaredPercent = null,
            kind = IngredientKind.PLAIN,
            contains = contains,
        )

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf(*extra))
    }
}
