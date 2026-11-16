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
import pl.morning.tower.karbo.core.common.onFailure
import pl.morning.tower.karbo.core.common.onSuccess
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.report.api.ReportPhotoStore
import pl.morning.tower.karbo.feature.report.api.ReportRepository
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureDelegate
import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureResources
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnBackToReportClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnCapture
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnCloseClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnHomeClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnScanNextClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnSendClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnSlotClicked
import pl.morning.tower.karbo.feature.report.ui.event.MissingProductAction.OnTryAgainClicked
import pl.morning.tower.karbo.feature.report.ui.event.ReportEvent
import pl.morning.tower.karbo.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateBack
import pl.morning.tower.karbo.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToHome
import pl.morning.tower.karbo.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductState
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductStateFactory
import pl.morning.tower.karbo.feature.report.ui.state.PhotoSlot
import pl.morning.tower.karbo.feature.report.ui.state.ReportStatus
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductStatePrototype as Prototype

internal class MissingProductViewModel(
    route: ReportRoute,
    private val savedStateHandle: SavedStateHandle,
    private val reports: ReportRepository,
    private val photoStore: ReportPhotoStore,
    appEvents: AppEvents,
    private val stateFactory: MissingProductStateFactory,
    captureResources: PhotoCaptureResources,
    private val errorReporter: ErrorReporter,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            barcode = Barcode(route.barcode),
            slotPhotos = restoredSlotPhotos(),
            captureSlot = savedStateHandle.get<String>(KEY_CAPTURE_SLOT)?.let(PhotoSlot::valueOf) ?: PhotoSlot.FRONT,
            status = restoredStatus(),
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
        onPhotoAccepted = ::fillSlot,
    )

    val state: StateFlow<MissingProductState> = combine(
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

    private val screenEvents = EventQueue<ReportEvent>(viewModelScope)
    val events: Flow<ReportEvent> = screenEvents.events

    val captureEvents: Flow<PhotoCaptureEvent> = capture.events

    fun onAction(action: MissingProductAction) {
        when (action) {
            OnCloseClicked -> screenEvents send NavigateBack
            is OnSlotClicked -> onSlotClicked(action.slot)
            OnSendClicked -> onSendClicked()
            OnTryAgainClicked -> onTryAgainClicked()
            OnBackToReportClicked -> onBackToReportClicked()
            OnHomeClicked -> screenEvents send NavigateToHome
            OnScanNextClicked -> screenEvents send NavigateToScan
            is OnCapture -> capture.onAction(action.action)
        }
    }

    override fun onCleared() {
        deletePhotos(statePrototype.value.photos)
        capture.discard()
    }

    private fun cleanUpPhotos() {
        val slotPhotos = statePrototype.value.photos
        val restored = slotPhotos + capture.photos
        viewModelScope.launch {
            photoStore.retainOnly(restored)
                .onSuccess { existing ->
                    val existingSet = existing.toSet()
                    setSlotPhotos(statePrototype.value.slotPhotos.filterValues { it in existingSet })
                    capture.keepOnly(existing)
                }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Cleaning up product photos failed", error = error)
                }
        }
    }

    private fun onSlotClicked(slot: PhotoSlot) {
        if (!isEditing() || capture.isOpen) return
        savedStateHandle[KEY_CAPTURE_SLOT] = slot.name
        statePrototype.update { it.copy(captureSlot = slot) }
        capture.open()
    }

    private fun fillSlot(photo: ReportPhoto) {
        if (!isEditing()) {
            deletePhotos(listOf(photo))
            return
        }
        val prototype = statePrototype.value
        val replaced = prototype.slotPhotos[prototype.captureSlot]
        setSlotPhotos(prototype.slotPhotos + (prototype.captureSlot to photo))
        if (replaced != null && replaced != photo) {
            deletePhotos(listOf(replaced))
        }
    }

    private fun onSendClicked() {
        if (!isEditing() || statePrototype.value.toReport() == null) return
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

    private fun send() {
        val report = statePrototype.value.toReport() ?: return
        setStatus(ReportStatus.SENDING)
        viewModelScope.launch {
            reports.submitMissingProduct(report)
                .onSuccess {
                    setStatus(ReportStatus.SENT)
                    val sentPhotos = statePrototype.value.photos
                    setSlotPhotos(emptyMap())
                    deletePhotos(sentPhotos)
                }
                .onFailure { error ->
                    setStatus(ReportStatus.FAILED)
                    errorReporter.report(tag = TAG, message = "Sending missing product failed", error = error)
                }
        }
    }

    private fun deletePhotos(photos: List<ReportPhoto>) {
        if (photos.isEmpty()) return
        applicationScope.launch {
            photoStore.delete(photos).onFailure { error ->
                errorReporter.report(tag = TAG, message = "Deleting product photos failed", error = error)
            }
        }
    }

    private fun setSlotPhotos(slotPhotos: Map<PhotoSlot, ReportPhoto>) {
        PhotoSlot.entries.forEach { slot ->
            savedStateHandle[slot.key] = slotPhotos[slot]?.uri
        }
        statePrototype.update { it.copy(slotPhotos = slotPhotos) }
    }

    private fun isEditing(): Boolean = statePrototype.value.status == ReportStatus.EDITING

    private fun setStatus(status: ReportStatus) {
        savedStateHandle[KEY_STATUS] = status.name
        statePrototype.update { it.copy(status = status) }
    }

    private fun restoredSlotPhotos(): Map<PhotoSlot, ReportPhoto> = PhotoSlot.entries
        .mapNotNull { slot -> savedStateHandle.get<String>(slot.key)?.let { uri -> slot to ReportPhoto(uri) } }
        .toMap()

    private fun restoredStatus(): ReportStatus =
        when (val status = savedStateHandle.get<String>(KEY_STATUS)?.let(ReportStatus::valueOf)) {
            null, ReportStatus.SENDING -> ReportStatus.EDITING
            else -> status
        }

    private val PhotoSlot.key: String get() = "$KEY_PHOTO_PREFIX$name"

    init {
        cleanUpPhotos()
    }

    private companion object {
        const val TAG = "MissingProductViewModel"
        const val KEY_PHOTO_PREFIX = "photo_"
        const val KEY_CAPTURE_SLOT = "captureSlot"
        const val KEY_STATUS = "status"
    }
}
