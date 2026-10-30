package pl.handsome.club.feature.scanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnDetailsClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.handsome.club.feature.scanner.ui.event.ScanAction.OnScanNextClicked
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
    private val analyzeProduct: AnalyzeProduct,
    private val history: AnalysisHistoryRepository,
    private val appEvents: AppEvents,
    private val stateFactory: ScanStateFactory,
    private val resources: ScanResources,
    private val logger: AppLogger,
    private val errorReporter: ErrorReporter,
    private val applicationScope: CoroutineScope,
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
            foundBarcode = savedStateHandle.get<String>(KEY_FOUND_BARCODE),
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
            OnScanNextClicked -> scanNext()
            OnDetailsClicked -> statePrototype.value.foundBarcode?.let { screenEvents send NavigateToProduct(it) }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            progress.inProgress.collect { isSearching ->
                statePrototype.update { it.copy(isSearching = isSearching) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFoundAnalysis() {
        viewModelScope.launch {
            statePrototype
                .map { it.foundBarcode }
                .distinctUntilChanged()
                .flatMapLatest { barcode ->
                    if (barcode == null) flowOf(null)
                    else analyzeProduct(barcode).catch { onFoundAnalysisFailed() }
                }
                .collect { analysis ->
                    statePrototype.update { it.copy(foundAnalysis = analysis.takeIf { _ -> it.foundBarcode != null }) }
                }
        }
    }

    private fun onFoundAnalysisFailed() {
        reportOrLog("Observing found product failed", AppError.Storage)
        setFoundBarcode(null)
        viewModelScope.launch { showFailureAndResume(AppError.Storage) }
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

    private fun setFoundBarcode(barcode: String?) {
        savedStateHandle[KEY_FOUND_BARCODE] = barcode
        statePrototype.update { it.copy(foundBarcode = barcode, foundAnalysis = null) }
    }

    private fun scanNext() {
        setFoundBarcode(null)
        statePrototype.update { it.copy(isScanPaused = false) }
    }

    private fun search(barcode: String) {
        if (barcode.isBlank() || progress.inProgress.value || statePrototype.value.foundBarcode != null) return
        statePrototype.update { it.copy(isScanPaused = true) }
        appEvents send HideKeyboard

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            progress.reportWork { products.refreshProduct(barcode) }
                .onSuccess {
                    setTypeSheetOpen(false)
                    setFoundBarcode(barcode)
                    saveFoundAnalysisToHistory()
                }
                .onFailure { error -> onSearchFailed(error) }
        }
    }

    private suspend fun saveFoundAnalysisToHistory() {
        val analysis = statePrototype
            .first { it.foundBarcode == null || it.foundAnalysis != null }
            .foundAnalysis ?: return
        applicationScope.launch {
            history.save(analysis).onFailure { error ->
                logger.error(TAG, "Saving history entry failed: $error", (error as? AppError.Unknown)?.cause)
            }
        }
    }

    private suspend fun onSearchFailed(error: AppError) {
        reportOrLog("Searching product failed", error)
        showFailureAndResume(error)
    }

    private suspend fun showFailureAndResume(error: AppError) {
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
        observeFoundAnalysis()
    }

    private companion object {
        const val TAG = "ScanViewModel"
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val KEY_TYPE_SHEET_OPEN = "typeSheetOpen"
        const val KEY_FOUND_BARCODE = "foundBarcode"
        val SCAN_RESUME_DELAY = 2.seconds
    }
}
