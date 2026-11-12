package pl.morning.tower.karbo.feature.scanner.ui

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
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.AppLogger
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.common.EventQueue
import pl.morning.tower.karbo.core.common.ProgressTracker
import pl.morning.tower.karbo.core.common.catchAppError
import pl.morning.tower.karbo.core.common.isReportable
import pl.morning.tower.karbo.core.common.onFailure
import pl.morning.tower.karbo.core.common.onSuccess
import pl.morning.tower.karbo.core.common.retryOnAppError
import pl.morning.tower.karbo.core.designsystem.component.MessageTone
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.HideKeyboard
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.ShowMessage
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.product.api.ProductRepository
import pl.morning.tower.karbo.feature.scanner.ui.event.OpenAppSettings
import pl.morning.tower.karbo.feature.scanner.ui.event.PerformHapticFeedback
import pl.morning.tower.karbo.feature.scanner.ui.event.RequestCameraPermission
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnDetailsClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnPermissionButtonClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnQueryChanged
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnScanNextClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnSearchSubmitted
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnStart
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTorchClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTripItemClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTypeSheetDismissed
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanEvent
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToProduct
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanFeedback
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraPermission
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraPermissionCheck
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanState
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanStateFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanStatePrototype as Prototype

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
    private val preferences: DietPreferencesRepository,
    private val clock: Clock,
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
            notFoundBarcode = savedStateHandle.get<String>(KEY_NOT_FOUND_BARCODE),
            now = clock.now(),
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
            OnSearchSubmitted -> search(statePrototype.value.query.trim(), fromCamera = false)
            is OnBarcodeScanned -> {
                if (!statePrototype.value.isScanBlocked) search(action.barcode, fromCamera = true)
            }
            OnTorchClicked -> toggleTorch()
            OnTypeItClicked -> openTypeSheet()
            OnTypeSheetDismissed -> setTypeSheetOpen(false)
            OnScanNextClicked -> scanNext()
            OnDetailsClicked -> statePrototype.value.foundBarcode?.let { screenEvents send NavigateToProduct(it) }
            is OnTripItemClicked -> screenEvents send NavigateToProduct(action.barcode)
        }
    }

    private fun observeTrip() {
        viewModelScope.launch {
            history.observeLastUniqueEntries(Prototype.TRIP_LIMIT)
                .retryOnAppError(::onTripFailed)
                .collect { entries ->
                    statePrototype.update { it.copy(tripEntries = entries, now = clock.now()) }
                }
        }
    }

    private suspend fun onTripFailed(error: AppError) {
        errorReporter.report(TAG, "Loading trip entries failed", error)
        delay(TRIP_RETRY_DELAY)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.observeDietPreferences()
                .catchAppError { error -> errorReporter.report(TAG, "Loading diet preferences failed", error) }
                .collect { dietPreferences ->
                    statePrototype.update { it.copy(dietPreferences = dietPreferences) }
                }
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
        statePrototype.update {
            it.copy(isScanPaused = progress.inProgress.value, hasTorch = hasTorch, now = clock.now())
        }
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

    private fun openTypeSheet() {
        statePrototype.value.notFoundBarcode?.let { barcode ->
            setQuery(barcode)
            setNotFoundBarcode(null)
            statePrototype.update { it.copy(isScanPaused = false) }
        }
        setTypeSheetOpen(true)
    }

    private fun setFoundBarcode(barcode: String?) {
        savedStateHandle[KEY_FOUND_BARCODE] = barcode
        statePrototype.update { it.copy(foundBarcode = barcode, foundAnalysis = null) }
    }

    private fun setNotFoundBarcode(barcode: String?) {
        savedStateHandle[KEY_NOT_FOUND_BARCODE] = barcode
        statePrototype.update { it.copy(notFoundBarcode = barcode) }
    }

    private fun scanNext() {
        setFoundBarcode(null)
        setNotFoundBarcode(null)
        statePrototype.update { it.copy(isScanPaused = false) }
    }

    private fun search(barcode: String, fromCamera: Boolean) {
        val prototype = statePrototype.value
        if (barcode.isBlank() || progress.inProgress.value) return
        if (prototype.foundBarcode != null || prototype.notFoundBarcode != null) return
        statePrototype.update { it.copy(isScanPaused = true) }
        if (fromCamera) screenEvents send PerformHapticFeedback(ScanFeedback.Detected)
        appEvents send HideKeyboard

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            progress.reportWork { products.refreshProduct(barcode) }
                .onSuccess {
                    setTypeSheetOpen(false)
                    setFoundBarcode(barcode)
                    saveFoundAnalysisToHistory()
                }
                .onFailure { error -> onSearchFailed(barcode, error) }
        }
    }

    private suspend fun saveFoundAnalysisToHistory() {
        val analysis = statePrototype
            .first { it.foundBarcode == null || it.foundAnalysis != null }
            .foundAnalysis ?: return
        screenEvents send PerformHapticFeedback(ScanFeedback.Found)
        applicationScope.launch {
            history.save(analysis.product).onFailure { error ->
                logger.error(TAG, "Saving history entry failed: $error", (error as? AppError.Unknown)?.cause)
            }
        }
    }

    private suspend fun onSearchFailed(barcode: String, error: AppError) {
        reportOrLog("Searching product failed", error)
        if (error == AppError.NotFound) {
            setTypeSheetOpen(false)
            setNotFoundBarcode(barcode)
            screenEvents send PerformHapticFeedback(ScanFeedback.NotFound)
        } else {
            showFailureAndResume(error)
        }
    }

    private suspend fun showFailureAndResume(error: AppError) {
        appEvents send ShowMessage(resources.searchFailed(error), MessageTone.Error)
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
        observeTrip()
        observePreferences()
    }

    private companion object {
        const val TAG = "ScanViewModel"
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val KEY_TYPE_SHEET_OPEN = "typeSheetOpen"
        const val KEY_FOUND_BARCODE = "foundBarcode"
        const val KEY_NOT_FOUND_BARCODE = "notFoundBarcode"
        val SCAN_RESUME_DELAY = 2.seconds
        val TRIP_RETRY_DELAY = 5.seconds
    }
}
