package app.carbpeek.feature.report.ui.capture

import androidx.lifecycle.SavedStateHandle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.onFailure
import app.carbpeek.core.common.onSuccess
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent.ShowMessage
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.report.api.ReportPhotoStore
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraPermissionResult
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnGalleryClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPermissionButtonClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPhotoPicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureFailed
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnRetakeClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnShutterClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnTorchClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnUsePhotoClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenAppSettings
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenPhotoPicker
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.RequestCameraPermission
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.TakePicture
import app.carbpeek.feature.report.ui.capture.state.CameraPermission
import app.carbpeek.feature.report.ui.capture.state.CameraPermissionCheck
import app.carbpeek.feature.report.ui.capture.state.PhotoCapturePrototype
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PhotoCaptureDelegate(
    private val savedStateHandle: SavedStateHandle,
    private val photoStore: ReportPhotoStore,
    private val appEvents: AppEvents,
    private val resources: PhotoCaptureResources,
    private val errorReporter: ErrorReporter,
    private val scope: CoroutineScope,
    private val applicationScope: CoroutineScope,
    private val onPhotoAccepted: (ReportPhoto) -> Unit,
) {

    private val prototype = MutableStateFlow(
        PhotoCapturePrototype(
            isOpen = savedStateHandle.get<Boolean>(KEY_OPEN) ?: false,
            cameraPermission = savedStateHandle.get<String>(KEY_CAMERA_PERMISSION)
                ?.let(CameraPermission::valueOf)
                ?.takeUnless { it == CameraPermission.Granted }
                ?: CameraPermission.Unchecked,
            isTorchOn = savedStateHandle.get<Boolean>(KEY_TORCH) ?: false,
            previewPhoto = savedStateHandle.get<String>(KEY_PREVIEW_PHOTO)?.let(::ReportPhoto),
        )
    )

    val state: StateFlow<PhotoCapturePrototype> = prototype.asStateFlow()

    private val captureEvents = EventQueue<PhotoCaptureEvent>(scope)
    val events: Flow<PhotoCaptureEvent> = captureEvents.events

    val isOpen: Boolean get() = prototype.value.isOpen

    val photos: List<ReportPhoto> get() = prototype.value.photos

    fun open() {
        if (isOpen) return
        setOpen(true)
        setTorch(false)
    }

    fun onAction(action: PhotoCaptureAction) {
        when (action) {
            is OnCameraChecked -> onCameraChecked(action.check, action.hasTorch)
            is OnCameraPermissionResult -> onCameraPermissionResult(action.check)
            OnPermissionButtonClicked -> onPermissionButtonClicked()
            OnTorchClicked -> onTorchClicked()
            OnShutterClicked -> onShutterClicked()
            is OnPictureSaved -> onPictureSaved(action.uri)
            is OnPictureFailed -> onPictureFailed(action.uri, action.error)
            OnGalleryClicked -> onGalleryClicked()
            is OnPhotoPicked -> importPhoto(action.uri)
            OnRetakeClicked -> retake()
            OnUsePhotoClicked -> usePhoto()
            OnCancelClicked -> cancel()
        }
    }

    fun keepOnly(existing: List<ReportPhoto>) {
        val preview = prototype.value.previewPhoto ?: return
        if (preview !in existing) {
            setPreviewPhoto(null)
        }
    }

    fun discard() {
        deletePhotos(photos)
    }

    private fun onCameraChecked(check: CameraPermissionCheck, hasTorch: Boolean) {
        if (!isOpen) return
        prototype.update { it.copy(hasTorch = hasTorch) }
        val current = prototype.value.cameraPermission
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
        if (!isOpen) return
        when (prototype.value.cameraPermission) {
            CameraPermission.Rationale -> requestCameraPermission()
            CameraPermission.Denied -> captureEvents send OpenAppSettings
            CameraPermission.Unchecked, CameraPermission.Requesting, CameraPermission.Granted -> Unit
        }
    }

    private fun requestCameraPermission() {
        setCameraPermission(CameraPermission.Requesting)
        captureEvents send RequestCameraPermission
    }

    private fun onTorchClicked() {
        val capture = prototype.value
        if (!capture.showsCamera() || !capture.hasTorch) return
        setTorch(!capture.isTorchOn)
    }

    private fun onShutterClicked() {
        val capture = prototype.value
        if (!capture.showsCamera() || capture.isCapturing) return
        setCapturing(true)
        scope.launch {
            photoStore.newPhoto()
                .onSuccess { photo ->
                    captureEvents send TakePicture(photo.uri)
                }
                .onFailure { error ->
                    setCapturing(false)
                    showError(resources.photoCaptureFailed())
                    errorReporter.report(tag = TAG, message = "Creating a photo file failed", error = error)
                }
        }
    }

    private fun onPictureSaved(uri: String) {
        val capture = prototype.value
        val photo = ReportPhoto(uri)
        if (capture.isOpen && capture.isCapturing && capture.previewPhoto == null) {
            setPreviewPhoto(photo)
        } else {
            deletePhotos(listOf(photo))
        }
        setCapturing(false)
    }

    private fun onPictureFailed(uri: String, error: AppError) {
        deletePhotos(listOf(ReportPhoto(uri)))
        setCapturing(false)
        showError(resources.photoCaptureFailed())
        errorReporter.report(tag = TAG, message = "Taking a photo failed", error = error)
    }

    private fun onGalleryClicked() {
        if (!canPickPhoto()) return
        captureEvents send OpenPhotoPicker
    }

    private fun importPhoto(uri: String) {
        if (!canPickPhoto()) return
        scope.launch {
            photoStore.importPhoto(uri)
                .onSuccess { photo ->
                    if (canPickPhoto()) {
                        setPreviewPhoto(photo)
                    } else {
                        deletePhotos(listOf(photo))
                    }
                }
                .onFailure { error ->
                    showError(resources.photoImportFailed())
                    errorReporter.report(tag = TAG, message = "Importing a photo failed", error = error)
                }
        }
    }

    private fun usePhoto() {
        val preview = prototype.value.previewPhoto ?: return
        onPhotoAccepted(preview)
        setPreviewPhoto(null)
        close()
    }

    private fun retake() {
        val preview = prototype.value.previewPhoto ?: return
        deletePhotos(listOf(preview))
        setPreviewPhoto(null)
    }

    private fun cancel() {
        val capture = prototype.value
        if (!capture.isOpen || capture.isCapturing) return
        deletePhotos(capture.photos)
        setPreviewPhoto(null)
        close()
    }

    private fun close() {
        setOpen(false)
        setTorch(false)
    }

    private fun canPickPhoto(): Boolean {
        val capture = prototype.value
        return capture.isOpen && capture.previewPhoto == null && !capture.isCapturing
    }

    private fun PhotoCapturePrototype.showsCamera(): Boolean =
        isOpen && cameraPermission == CameraPermission.Granted && previewPhoto == null

    private fun showError(text: UiText) {
        appEvents send ShowMessage(text = text, tone = MessageTone.Error)
    }

    private fun deletePhotos(photos: List<ReportPhoto>) {
        if (photos.isEmpty()) return
        applicationScope.launch {
            photoStore.delete(photos).onFailure { error ->
                errorReporter.report(tag = TAG, message = "Deleting photos failed", error = error)
            }
        }
    }

    private fun setOpen(isOpen: Boolean) {
        savedStateHandle[KEY_OPEN] = isOpen
        prototype.update { it.copy(isOpen = isOpen) }
    }

    private fun setCameraPermission(permission: CameraPermission) {
        savedStateHandle[KEY_CAMERA_PERMISSION] = permission.name
        prototype.update { it.copy(cameraPermission = permission) }
    }

    private fun setTorch(isOn: Boolean) {
        savedStateHandle[KEY_TORCH] = isOn
        prototype.update { it.copy(isTorchOn = isOn) }
    }

    private fun setPreviewPhoto(photo: ReportPhoto?) {
        savedStateHandle[KEY_PREVIEW_PHOTO] = photo?.uri
        prototype.update { it.copy(previewPhoto = photo) }
    }

    private fun setCapturing(isCapturing: Boolean) {
        prototype.update { it.copy(isCapturing = isCapturing) }
    }

    private companion object {
        const val TAG = "PhotoCaptureDelegate"
        const val KEY_OPEN = "captureOpen"
        const val KEY_PREVIEW_PHOTO = "previewPhoto"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
    }
}
