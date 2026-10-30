package pl.handsome.club.feature.scanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
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
import pl.handsome.club.core.common.ErrorReporter
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.ProgressTracker
import pl.handsome.club.core.common.isReportable
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.designsystem.event.AppEvent.HideKeyboard
import pl.handsome.club.core.designsystem.event.AppEvent.ShowMessage
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnSearchSubmitted
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnStart
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTorchClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnTypeSheetDismissed
import pl.handsome.club.feature.scanner.ui.event.ScanEvent
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToProduct
import pl.handsome.club.feature.scanner.ui.state.CameraPermission
import pl.handsome.club.feature.scanner.ui.state.CameraPermissionCheck
import pl.handsome.club.feature.scanner.ui.state.ScanState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import kotlin.time.Duration.Companion.seconds
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

internal class ScanViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val products: ProductRepository,
    private val appEvents: AppEvents,
    private val stateFactory: ScanStateFactory,
    private val resources: ScanResources,
    private val logger: AppLogger,
    private val errorReporter: ErrorReporter,
    initialQuery: String,
) : ViewModel() {

    private val progress = ProgressTracker()

    private val statePrototype = MutableStateFlow(
        Prototype(
            query = savedStateHandle.get<String>(KEY_QUERY) ?: initialQuery,
            cameraPermission = savedStateHandle.get<String>(KEY_CAMERA_PERMISSION)
                ?.let(CameraPermission::valueOf)
                ?.takeUnless { it == CameraPermission.Granted }
                ?: CameraPermission.Unchecked,
            isTorchOn = savedStateHandle.get<Boolean>(KEY_TORCH) ?: false,
            isTypeSheetOpen = savedStateHandle.get<Boolean>(KEY_TYPE_SHEET_OPEN) ?: false,
        )
    )

    val state: StateFlow<ScanState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<ScanEvent>(viewModelScope)
    val events: Flow<ScanEvent> = screenEvents.events

    fun onAction(action: ScanAction) {
        when (action) {
            is OnStart -> onStart(action.check, action.hasTorch)
            is OnCameraPermissionResult -> onCameraPermissionResult(action.check)
            OnPermissionButtonClicked -> onPermissionButtonClicked()
            is OnQueryChanged -> setQuery(action.query)
            OnSearchSubmitted -> search(statePrototype.value.query.trim())
            is OnBarcodeScanned -> if (!statePrototype.value.isScanBlocked) search(action.barcode)
            OnTorchClicked -> toggleTorch()
            OnTypeItClicked -> setTypeSheetOpen(true)
            OnTypeSheetDismissed -> setTypeSheetOpen(false)
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            progress.inProgress.collect { isSearching ->
                statePrototype.update { it.copy(isSearching = isSearching) }
            }
        }
    }

    private fun onStart(check: CameraPermissionCheck, hasTorch: Boolean) {
        statePrototype.update { it.copy(isScanPaused = progress.inProgress.value, hasTorch = hasTorch) }
        val current = statePrototype.value.cameraPermission
        when {
            check.isGranted -> setCameraPermission(CameraPermission.Granted)
            current == CameraPermission.Denied || current == CameraPermission.Requesting -> Unit
            check.shouldShowRationale -> setCameraPermission(CameraPermission.Rationale)
            else -> requestCameraPermission()
        }
    }

    private fun onCameraPermissionResult(check: CameraPermissionCheck) {
        val permission = when {
            check.isGranted -> CameraPermission.Granted
            check.shouldShowRationale -> CameraPermission.Rationale
            else -> CameraPermission.Denied
        }
        setCameraPermission(permission)
    }

    private fun onPermissionButtonClicked() {
        when (statePrototype.value.cameraPermission) {
            CameraPermission.Rationale -> requestCameraPermission()
            CameraPermission.Denied -> screenEvents send OpenAppSettings
            CameraPermission.Unchecked, CameraPermission.Requesting, CameraPermission.Granted -> Unit
        }
    }

    private fun requestCameraPermission() {
        setCameraPermission(CameraPermission.Requesting)
        screenEvents send RequestCameraPermission
    }

    private fun setCameraPermission(permission: CameraPermission) {
        savedStateHandle[KEY_CAMERA_PERMISSION] = permission.name
        statePrototype.update { it.copy(cameraPermission = permission) }
    }

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        statePrototype.update { it.copy(query = query) }
    }

    private fun toggleTorch() {
        val isTorchOn = !statePrototype.value.isTorchOn
        savedStateHandle[KEY_TORCH] = isTorchOn
        statePrototype.update { it.copy(isTorchOn = isTorchOn) }
    }

    private fun setTypeSheetOpen(isOpen: Boolean) {
        savedStateHandle[KEY_TYPE_SHEET_OPEN] = isOpen
        statePrototype.update { it.copy(isTypeSheetOpen = isOpen) }
    }

    private fun search(barcode: String) {
        if (barcode.isBlank() || progress.inProgress.value) return
        statePrototype.update { it.copy(isScanPaused = true) }
        appEvents send HideKeyboard

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            progress.reportWork { products.refreshProduct(barcode) }
                .onSuccess {
                    setTypeSheetOpen(false)
                    screenEvents send NavigateToProduct(barcode)
                }
                .onFailure { error -> onSearchFailed(error) }
        }
    }

    private suspend fun onSearchFailed(error: AppError) {
        reportOrLog("Searching product failed", error)
        appEvents send ShowMessage(resources.searchFailed(error))
        delay(SCAN_RESUME_DELAY)
        statePrototype.update { it.copy(isScanPaused = false) }
    }

    private fun reportOrLog(message: String, error: AppError) {
        if (error.isReportable) errorReporter.report(TAG, message, error)
        else logger.warn(TAG, "$message: $error")
    }

    init {
        observeProgress()
    }

    private companion object {
        const val TAG = "ScanViewModel"
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val KEY_TYPE_SHEET_OPEN = "typeSheetOpen"
        val SCAN_RESUME_DELAY = 2.seconds
    }
}
