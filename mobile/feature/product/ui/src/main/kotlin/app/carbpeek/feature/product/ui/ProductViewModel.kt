package app.carbpeek.feature.product.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.onFailure
import app.carbpeek.core.common.onSuccess
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent.ShowMessage
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.favourites.api.FavouriteProductsRepository
import app.carbpeek.feature.product.api.ProductRepository
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnAddPhotosClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnBackClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnFavouriteClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnImageClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientBackClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientContentClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientSheetDismissed
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoDismissed
import app.carbpeek.feature.product.ui.event.ProductAction.OnNameClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnPer100gClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnPerServingClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnReportClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnRetryClicked
import app.carbpeek.feature.product.ui.event.ProductEvent
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateBack
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateToAddProduct
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation.NavigateToReport
import app.carbpeek.feature.product.ui.state.ProductState
import app.carbpeek.feature.product.ui.state.ProductStateFactory
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
import kotlin.coroutines.cancellation.CancellationException
import app.carbpeek.feature.product.ui.state.ProductStatePrototype as Prototype

internal class ProductViewModel(
    private val route: ProductRoute,
    private val savedStateHandle: SavedStateHandle,
    private val analyzeProduct: AnalyzeProduct,
    private val products: ProductRepository,
    private val favourites: FavouriteProductsRepository,
    private val appEvents: AppEvents,
    private val stateFactory: ProductStateFactory,
    private val resources: ProductResources,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            barcode = route.barcode,
            isImagePreviewOpen = savedStateHandle.get<Boolean>(KEY_IMAGE_PREVIEW_OPEN) ?: false,
            isNameExpanded = savedStateHandle.get<Boolean>(KEY_NAME_EXPANDED) ?: false,
            isPerServingSelected = savedStateHandle.get<Boolean>(KEY_PER_SERVING_SELECTED) ?: false,
            openIngredientIndex = savedStateHandle.get<Int>(KEY_OPEN_INGREDIENT),
            openIngredientContentIndex = savedStateHandle.get<Int>(KEY_OPEN_INGREDIENT_CONTENT),
            isIngredientsInfoOpen = savedStateHandle.get<Boolean>(KEY_INGREDIENTS_INFO_OPEN) ?: false,
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
            OnAddPhotosClicked -> screenEvents send NavigateToAddProduct(statePrototype.value.barcode)
            OnReportClicked -> screenEvents send NavigateToReport(statePrototype.value.barcode)
            is OnIngredientClicked -> setOpenIngredient(index = action.index, contentIndex = null)
            is OnIngredientContentClicked -> setOpenIngredient(
                index = statePrototype.value.openIngredientIndex,
                contentIndex = action.index,
            )
            OnIngredientBackClicked -> setOpenIngredient(
                index = statePrototype.value.openIngredientIndex,
                contentIndex = null,
            )
            OnIngredientSheetDismissed -> setOpenIngredient(index = null, contentIndex = null)
            OnIngredientsInfoClicked -> setIngredientsInfoOpen(true)
            OnIngredientsInfoDismissed -> setIngredientsInfoOpen(false)
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

    private fun loadFavouriteStatus() {
        viewModelScope.launch {
            val analysis = firstShownAnalysis()
            favourites.isFavourite(Barcode(analysis.product.barcode))
                .onSuccess { isFavourite ->
                    statePrototype.update { it.copy(isFavourite = isFavourite) }
                }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Loading favourite status failed", error = error)
                }
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
            errorReporter.report(tag = TAG, message = "Loading product failed", error = error)
        } else {
            errorReporter.report(tag = TAG, message = "Refreshing product failed", error = error)
        }
    }

    private fun onObserveFailed() {
        errorReporter.report(tag = TAG, message = "Observing product failed", error = AppError.Storage)
        if (statePrototype.value.analysis == null) {
            statePrototype.update { it.copy(loadError = AppError.Storage) }
        }
    }

    private fun toggleFavourite() {
        val prototype = statePrototype.value
        val isFavourite = prototype.isFavourite ?: return
        val analysis = prototype.analysis ?: return
        if (prototype.isFavouriteUpdating) return
        statePrototype.update { it.copy(isFavouriteUpdating = true) }

        viewModelScope.launch {
            val outcome = if (isFavourite) favourites.removeFromFavourites(analysis.product)
            else favourites.addToFavourites(analysis.product)

            outcome
                .onSuccess {
                    statePrototype.update { it.copy(isFavourite = !isFavourite, isFavouriteUpdating = false) }
                    val message = if (isFavourite) resources.removedFromFavourites() else resources.addedToFavourites()
                    appEvents send ShowMessage(message, MessageTone.Success)
                }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Updating favourites failed", error = error)
                    statePrototype.update { it.copy(isFavouriteUpdating = false) }
                    appEvents send ShowMessage(resources.error(error), MessageTone.Error)
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

    private fun setOpenIngredient(index: Int?, contentIndex: Int?) {
        savedStateHandle[KEY_OPEN_INGREDIENT] = index
        savedStateHandle[KEY_OPEN_INGREDIENT_CONTENT] = contentIndex
        statePrototype.update { it.copy(openIngredientIndex = index, openIngredientContentIndex = contentIndex) }
    }

    private fun setIngredientsInfoOpen(isOpen: Boolean) {
        savedStateHandle[KEY_INGREDIENTS_INFO_OPEN] = isOpen
        statePrototype.update { it.copy(isIngredientsInfoOpen = isOpen) }
    }

    init {
        observeProduct()
        loadFavouriteStatus()
        refresh()
    }

    private companion object {
        const val TAG = "ProductViewModel"
        const val KEY_IMAGE_PREVIEW_OPEN = "imagePreviewOpen"
        const val KEY_NAME_EXPANDED = "nameExpanded"
        const val KEY_PER_SERVING_SELECTED = "perServingSelected"
        const val KEY_OPEN_INGREDIENT = "openIngredient"
        const val KEY_OPEN_INGREDIENT_CONTENT = "openIngredientContent"
        const val KEY_INGREDIENTS_INFO_OPEN = "ingredientsInfoOpen"
    }
}
