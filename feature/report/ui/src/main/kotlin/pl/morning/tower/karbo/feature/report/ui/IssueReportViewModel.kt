package pl.morning.tower.karbo.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.common.EventQueue
import pl.morning.tower.karbo.core.common.catchAppError
import pl.morning.tower.karbo.core.common.onFailure
import pl.morning.tower.karbo.core.common.onSuccess
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.HideKeyboard
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.report.api.ReportPhotoStore
import pl.morning.tower.karbo.feature.report.api.ReportRepository
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureDelegate
import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureResources
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnAddPhotoClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnBackToReportClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnCapture
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnCloseClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnDetailsChanged
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnExpectedRateClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnHomeClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnReasonClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnRemovePhotoClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnScanNextClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnSendClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnTryAgainClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateBack
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateToHome
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportState
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportStateFactory
import pl.morning.tower.karbo.feature.report.ui.state.MAX_REPORT_PHOTOS
import pl.morning.tower.karbo.feature.report.ui.state.ReportStatus
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportStatePrototype as Prototype

internal class IssueReportViewModel(
    route: ReportRoute,
    private val savedStateHandle: SavedStateHandle,
    private val analyzeProduct: AnalyzeProduct,
    private val dietPreferences: DietPreferencesRepository,
    private val reports: ReportRepository,
    private val photoStore: ReportPhotoStore,
    private val appEvents: AppEvents,
    private val stateFactory: IssueReportStateFactory,
    captureResources: PhotoCaptureResources,
    private val errorReporter: ErrorReporter,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            barcode = Barcode(route.barcode),
            reason = savedStateHandle.get<String>(KEY_REASON)?.let(IssueReason::valueOf),
            details = savedStateHandle.get<String>(KEY_DETAILS) ?: "",
            expectedRate = savedStateHandle.get<String>(KEY_EXPECTED_RATE)?.let(DietRate::valueOf),
            status = restoredStatus(),
            photos = savedStateHandle.get<ArrayList<String>>(KEY_PHOTOS).orEmpty().map(::ReportPhoto),
        ),
    )

    private val capture = PhotoCaptureDelegate(
        savedStateHandle = savedStateHandle,
        photoStore = photoStore,
        appEvents = appEvents,
        resources = captureResources,
        errorReporter = errorReporter,
        scope = viewModelScope,
        applicationScope = applicationScope,
        onPhotoAccepted = ::attachPhoto,
    )

    val state: StateFlow<IssueReportState> = combine(
        statePrototype,
        capture.state,
    ) { prototype, captureState ->
        stateFactory.createFor(prototype = prototype, capture = captureState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(
            prototype = statePrototype.value,
            capture = capture.state.value,
        ),
    )

    private val screenEvents = EventQueue<IssueReportEvent>(viewModelScope)
    val events: Flow<IssueReportEvent> = screenEvents.events

    val captureEvents: Flow<PhotoCaptureEvent> = capture.events

    fun onAction(action: IssueReportAction) {
        when (action) {
            OnCloseClicked -> screenEvents send NavigateBack
            is OnReasonClicked -> selectReason(action.reason)
            is OnDetailsChanged -> changeDetails(action.details)
            is OnExpectedRateClicked -> selectExpectedRate(action.rate)
            OnSendClicked -> onSendClicked()
            OnTryAgainClicked -> onTryAgainClicked()
            OnBackToReportClicked -> onBackToReportClicked()
            OnHomeClicked -> screenEvents send NavigateToHome
            OnScanNextClicked -> screenEvents send NavigateToScan
            OnAddPhotoClicked -> onAddPhotoClicked()
            is OnRemovePhotoClicked -> removePhoto(action.uri)
            is OnCapture -> capture.onAction(action.action)
        }
    }

    override fun onCleared() {
        deletePhotos(statePrototype.value.photos)
        capture.discard()
    }

    private fun cleanUpPhotos() {
        val attached = statePrototype.value.photos
        val restored = attached + capture.photos
        viewModelScope.launch {
            photoStore.retainOnly(restored)
                .onSuccess { existing ->
                    val missing = attached - existing.toSet()
                    setPhotos(statePrototype.value.photos - missing.toSet())
                    capture.keepOnly(existing)
                }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Cleaning up report photos failed", error = error)
                }
        }
    }

    private fun observeProduct() {
        viewModelScope.launch {
            analyzeProduct(statePrototype.value.barcode.value)
                .catchAppError { error ->
                    errorReporter.report(
                        tag = TAG,
                        message = "Observing product failed",
                        error = error,
                    )
                }
                .collect { analysis ->
                    statePrototype.update { it.copy(analysis = analysis) }
                }
        }
    }

    private fun observeDietMode() {
        viewModelScope.launch {
            dietPreferences.observeDietPreferences()
                .catchAppError { error ->
                    errorReporter.report(
                        tag = TAG,
                        message = "Observing diet preferences failed",
                        error = error,
                    )
                }
                .collect { preferences ->
                    statePrototype.update { it.copy(dietMode = preferences.dietMode) }
                }
        }
    }

    private fun selectReason(reason: IssueReason) {
        if (!isEditing()) return
        val selected = reason.takeUnless { it == statePrototype.value.reason }
        savedStateHandle[KEY_REASON] = selected?.name
        statePrototype.update { it.copy(reason = selected) }
    }

    private fun changeDetails(details: String) {
        if (!isEditing()) return
        val capped = details.take(MAX_DETAILS_LENGTH)
        savedStateHandle[KEY_DETAILS] = capped
        statePrototype.update { it.copy(details = capped) }
    }

    private fun selectExpectedRate(rate: DietRate) {
        if (!isEditing()) return
        val selected = rate.takeUnless { it == statePrototype.value.expectedRate }
        savedStateHandle[KEY_EXPECTED_RATE] = selected?.name
        statePrototype.update { it.copy(expectedRate = selected) }
    }

    private fun onSendClicked() {
        if (!isEditing() || statePrototype.value.toReport() == null) return
        appEvents send HideKeyboard
        send()
    }

    private fun onTryAgainClicked() {
        if (statePrototype.value.status != ReportStatus.FAILED) return
        send()
    }

    private fun onBackToReportClicked() {
        if (statePrototype.value.status != ReportStatus.FAILED) return
        setStatus(ReportStatus.EDITING)
    }

    private fun onAddPhotoClicked() {
        if (!isEditing() || !statePrototype.value.canAddPhoto || capture.isOpen) return
        appEvents send HideKeyboard
        capture.open()
    }

    private fun attachPhoto(photo: ReportPhoto) {
        val photos = statePrototype.value.photos
        if (isEditing() && photos.size < MAX_REPORT_PHOTOS) {
            setPhotos(photos + photo)
        } else {
            deletePhotos(listOf(photo))
        }
    }

    private fun removePhoto(uri: String) {
        if (!isEditing()) return
        val photo = statePrototype.value.photos.find { it.uri == uri } ?: return
        setPhotos(statePrototype.value.photos - photo)
        deletePhotos(listOf(photo))
    }

    private fun deletePhotos(photos: List<ReportPhoto>) {
        if (photos.isEmpty()) return
        applicationScope.launch {
            photoStore.delete(photos).onFailure { error ->
                errorReporter.report(tag = TAG, message = "Deleting report photos failed", error = error)
            }
        }
    }

    private fun setPhotos(photos: List<ReportPhoto>) {
        savedStateHandle[KEY_PHOTOS] = ArrayList(photos.map { it.uri })
        statePrototype.update { it.copy(photos = photos) }
    }

    private fun send() {
        val report = statePrototype.value.toReport() ?: return
        setStatus(ReportStatus.SENDING)
        viewModelScope.launch {
            reports.submitIssue(report)
                .onSuccess {
                    setStatus(ReportStatus.SENT)
                    val draftPhotos = statePrototype.value.photos
                    setPhotos(emptyList())
                    deletePhotos(draftPhotos)
                }
                .onFailure { error ->
                    setStatus(ReportStatus.FAILED)
                    errorReporter.report(tag = TAG, message = "Sending report failed", error = error)
                }
        }
    }

    private fun isEditing(): Boolean = statePrototype.value.status == ReportStatus.EDITING

    private fun setStatus(status: ReportStatus) {
        savedStateHandle[KEY_STATUS] = status.name
        statePrototype.update { it.copy(status = status) }
    }

    private fun restoredStatus(): ReportStatus =
        when (val status = savedStateHandle.get<String>(KEY_STATUS)?.let(ReportStatus::valueOf)) {
            null, ReportStatus.SENDING -> ReportStatus.EDITING
            else -> status
        }

    init {
        observeProduct()
        observeDietMode()
        cleanUpPhotos()
    }

    private companion object {
        const val TAG = "IssueReportViewModel"
        const val KEY_REASON = "reason"
        const val KEY_DETAILS = "details"
        const val KEY_EXPECTED_RATE = "expectedRate"
        const val KEY_STATUS = "status"
        const val KEY_PHOTOS = "photos"
        const val MAX_DETAILS_LENGTH = 1000
    }
}
