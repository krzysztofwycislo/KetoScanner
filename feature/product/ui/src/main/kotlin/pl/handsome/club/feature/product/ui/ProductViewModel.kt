package pl.handsome.club.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.AppLogger
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.designsystem.event.AppEvent.ShowMessage
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnBackClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnFavouriteClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImageClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import pl.handsome.club.feature.product.ui.event.ProductAction.OnNameClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnRetryClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnStart
import pl.handsome.club.feature.product.ui.event.ProductEvent
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateBack
import pl.handsome.club.feature.product.ui.state.ProductState
import pl.handsome.club.feature.product.ui.state.ProductStateFactory
import pl.handsome.club.feature.product.ui.state.ProductStatePrototype as Prototype

internal class ProductViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val analyzeProduct: AnalyzeProduct,
    private val favourites: FavouriteProductsRepository,
    private val history: AnalysisHistoryRepository,
    private val appEvents: AppEvents,
    private val stateFactory: ProductStateFactory,
    private val resources: ProductResources,
    private val logger: AppLogger,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            barcode = checkNotNull(savedStateHandle.get<String>(KEY_BARCODE)),
            isImagePreviewOpen = savedStateHandle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN) ?: false,
            isNameExpanded = savedStateHandle.get<Boolean>(KEY_NAME_EXPANDED) ?: false,
        )
    )

    val state: StateFlow<ProductState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<ProductEvent>(viewModelScope)
    val events: Flow<ProductEvent> = screenEvents.events

    fun onAction(action: ProductAction) {
        when (action) {
            OnStart -> load()
            OnRetryClicked -> load()
            OnBackClicked -> screenEvents send NavigateBack
            OnFavouriteClicked -> toggleFavourite()
            OnImageClicked -> openImagePreview()
            OnImagePreviewDismissed -> setImagePreviewOpen(false)
            OnNameClicked -> setNameExpanded(!statePrototype.value.isNameExpanded)
        }
    }

    private fun load() {
        val prototype = statePrototype.value
        if (prototype.isLoading || prototype.analysis != null) return
        statePrototype.update { it.copy(isLoading = true, loadError = null) }

        viewModelScope.launch {
            analyzeProduct(prototype.barcode)
                .onSuccess { analysis ->
                    statePrototype.update { it.copy(analysis = analysis, isLoading = false) }
                    saveToHistory(analysis)
                    loadFavouriteStatus(analysis)
                }
                .onFailure { error ->
                    log("Loading product failed", error)
                    statePrototype.update { it.copy(loadError = error, isLoading = false) }
                }
        }
    }

    private suspend fun saveToHistory(analysis: ProductAnalysis) {
        history.save(analysis).onFailure { error -> log("Saving history entry failed", error) }
    }

    private suspend fun loadFavouriteStatus(analysis: ProductAnalysis) {
        favourites.findByBarcode(analysis.product.barcode)
            .onSuccess { favourite -> statePrototype.update { it.copy(isFavourite = favourite != null) } }
            .onFailure { error -> log("Loading favourite status failed", error) }
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

    private fun log(message: String, error: AppError) {
        logger.error(TAG, "$message: $error", (error as? AppError.Unknown)?.cause)
    }

    private companion object {
        const val TAG = "ProductViewModel"
        const val KEY_BARCODE = "barcode"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"
    }
}
