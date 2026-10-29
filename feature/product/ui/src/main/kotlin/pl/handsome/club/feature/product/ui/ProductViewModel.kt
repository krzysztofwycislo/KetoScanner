package pl.handsome.club.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.AppLogger
import pl.handsome.club.core.common.ErrorReporter
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.isReportable
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.designsystem.event.AppEvent.ShowMessage
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnBackClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnFavouriteClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImageClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import pl.handsome.club.feature.product.ui.event.ProductAction.OnNameClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnOpenFoodFactsClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnPer100gClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnPerServingClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnRetryClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnWebPageUnavailable
import pl.handsome.club.feature.product.ui.event.ProductEvent
import pl.handsome.club.feature.product.ui.event.ProductEvent.OpenWebPage
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateBack
import pl.handsome.club.feature.product.ui.state.ProductState
import pl.handsome.club.feature.product.ui.state.ProductStateFactory
import pl.handsome.club.feature.product.ui.state.ProductStatePrototype as Prototype
import kotlin.coroutines.cancellation.CancellationException

internal class ProductViewModel(
    private val route: ProductRoute,
    private val savedStateHandle: SavedStateHandle,
    private val analyzeProduct: AnalyzeProduct,
    private val products: ProductRepository,
    private val favourites: FavouriteProductsRepository,
    private val history: AnalysisHistoryRepository,
    private val appEvents: AppEvents,
    private val stateFactory: ProductStateFactory,
    private val resources: ProductResources,
    private val logger: AppLogger,
    private val errorReporter: ErrorReporter,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            barcode = route.barcode,
            isImagePreviewOpen = savedStateHandle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN) ?: false,
            isNameExpanded = savedStateHandle.get<Boolean>(KEY_NAME_EXPANDED) ?: false,
            isPerServingSelected = savedStateHandle.get<Boolean>(KEY_PER_SERVING_SELECTED) ?: false,
        )
    )

    val state: StateFlow<ProductState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<ProductEvent>(viewModelScope)
    val events: Flow<ProductEvent> = screenEvents.events

    private val retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onAction(action: ProductAction) {
        when (action) {
            OnRetryClicked -> retry()
            OnBackClicked -> screenEvents send NavigateBack
            OnFavouriteClicked -> toggleFavourite()
            OnImageClicked -> openImagePreview()
            OnImagePreviewDismissed -> setImagePreviewOpen(false)
            OnNameClicked -> setNameExpanded(!statePrototype.value.isNameExpanded)
            OnPer100gClicked -> setPerServingSelected(false)
            OnPerServingClicked -> setPerServingSelected(true)
            OnOpenFoodFactsClicked -> screenEvents send OpenWebPage(PRODUCT_PAGE_URL + statePrototype.value.barcode)
            OnWebPageUnavailable -> appEvents send ShowMessage(resources.linkUnavailable())
        }
    }

    private fun observeProduct() {
        viewModelScope.launch {
            analyzeProduct(statePrototype.value.barcode)
                .retryWhen { error, _ ->
                    if (error is CancellationException) return@retryWhen false
                    onObserveFailed()
                    retryClicks.first()
                    true
                }
                .collect(::showAnalysis)
        }
    }

    private fun saveToHistory() {
        viewModelScope.launch {
            val analysis = firstShownAnalysis()
            applicationScope.launch {
                history.save(analysis).onFailure { error -> log("Saving history entry failed", error) }
            }
        }
    }

    private fun loadFavouriteStatus() {
        viewModelScope.launch {
            val analysis = firstShownAnalysis()
            favourites.findByBarcode(analysis.product.barcode)
                .onSuccess { favourite -> statePrototype.update { it.copy(isFavourite = favourite != null) } }
                .onFailure { error -> log("Loading favourite status failed", error) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            products.refreshProduct(statePrototype.value.barcode)
                .onFailure(::onRefreshFailed)
        }
    }

    private fun retry() {
        statePrototype.update { it.copy(loadError = null) }
        retryClicks.tryEmit(Unit)
        refresh()
    }

    private suspend fun firstShownAnalysis(): ProductAnalysis =
        statePrototype.mapNotNull { it.analysis }.first()

    private fun showAnalysis(analysis: ProductAnalysis) {
        statePrototype.update { it.copy(analysis = analysis, loadError = null) }
    }

    private fun onRefreshFailed(error: AppError) {
        if (statePrototype.value.analysis == null) {
            statePrototype.update { it.copy(loadError = error) }
            reportOrLog("Loading product failed", error)
        } else {
            reportOrLog("Refreshing product failed", error)
        }
    }

    private fun onObserveFailed() {
        reportOrLog("Observing product failed", AppError.Storage)
        if (statePrototype.value.analysis == null) {
            statePrototype.update { it.copy(loadError = AppError.Storage) }
        }
    }

    private fun toggleFavourite() {
        val prototype = statePrototype.value
        val isFavourite = prototype.isFavourite ?: return
        val product = prototype.analysis?.product ?: return
        if (prototype.isFavouriteUpdating) return
        statePrototype.update { it.copy(isFavouriteUpdating = true) }

        viewModelScope.launch {
            val outcome = if (isFavourite) favourites.removeFromFavourites(product)
            else favourites.addToFavourites(product)

            outcome
                .onSuccess {
                    statePrototype.update { it.copy(isFavourite = !isFavourite, isFavouriteUpdating = false) }
                    val message = if (isFavourite) resources.removedFromFavourites() else resources.addedToFavourites()
                    appEvents send ShowMessage(message)
                }
                .onFailure { error ->
                    log("Updating favourites failed", error)
                    statePrototype.update { it.copy(isFavouriteUpdating = false) }
                    appEvents send ShowMessage(resources.error(error))
                }
        }
    }

    private fun openImagePreview() {
        if (statePrototype.value.analysis?.product?.imageUrl == null) return
        setImagePreviewOpen(true)
    }

    private fun setImagePreviewOpen(isOpen: Boolean) {
        savedStateHandle[KEY_IMAGE_PREVIEW_OPEN] = isOpen
        statePrototype.update { it.copy(isImagePreviewOpen = isOpen) }
    }

    private fun setNameExpanded(isExpanded: Boolean) {
        savedStateHandle[KEY_NAME_EXPANDED] = isExpanded
        statePrototype.update { it.copy(isNameExpanded = isExpanded) }
    }

    private fun setPerServingSelected(isSelected: Boolean) {
        savedStateHandle[KEY_PER_SERVING_SELECTED] = isSelected
        statePrototype.update { it.copy(isPerServingSelected = isSelected) }
    }

    private fun log(message: String, error: AppError) {
        logger.error(TAG, "$message: $error", (error as? AppError.Unknown)?.cause)
    }

    private fun reportOrLog(message: String, error: AppError) {
        if (error.isReportable) errorReporter.report(TAG, message, error)
        else logger.warn(TAG, "$message: $error")
    }

    init {
        observeProduct()
        saveToHistory()
        loadFavouriteStatus()
        refresh()
    }

    private companion object {
        const val TAG = "ProductViewModel"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"
        const val KEY_PER_SERVING_SELECTED = "perServingSelected"
        const val PRODUCT_PAGE_URL = "https://world.openfoodfacts.org/product/"
    }
}
