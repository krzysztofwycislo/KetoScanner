package pl.morning.tower.karbo.photocamera

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executor

@Stable
class PhotoCameraState {

    internal var controller: LifecycleCameraController? = null

    internal var mainExecutor: Executor? = null

    fun takePicture(file: File, onSaved: () -> Unit, onFailed: (Throwable) -> Unit) {
        val controller = controller
        val executor = mainExecutor
        if (controller == null || executor == null) {
            onFailed(IllegalStateException("Camera is not attached"))
            return
        }
        try {
            controller.takePicture(
                ImageCapture.OutputFileOptions.Builder(file).build(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onSaved()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onFailed(exception)
                    }
                },
            )
        } catch (exception: IllegalStateException) {
            onFailed(exception)
        }
    }
}

@Composable
fun rememberPhotoCameraState(): PhotoCameraState = remember { PhotoCameraState() }

@RequiresPermission(Manifest.permission.CAMERA)
@Composable
fun PhotoCamera(
    state: PhotoCameraState,
    torchEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier)
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember(context) { createController(context) }
    val currentTorchEnabled by rememberUpdatedState(torchEnabled)

    DisposableEffect(controller, lifecycleOwner, state) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        controller.bindToLifecycle(lifecycleOwner)
        state.controller = controller
        state.mainExecutor = mainExecutor

        val torchObserver = Observer<CameraState> { cameraState ->
            if (cameraState.type == CameraState.Type.OPEN) controller.enableTorch(currentTorchEnabled)
        }
        var observedCameraState: LiveData<CameraState>? = null
        var disposed = false
        controller.initializationFuture.addListener(
            {
                if (!disposed) {
                    observedCameraState = controller.cameraInfo?.cameraState?.also { it.observeForever(torchObserver) }
                }
            },
            mainExecutor,
        )

        onDispose {
            disposed = true
            observedCameraState?.removeObserver(torchObserver)
            if (state.controller === controller) {
                state.controller = null
                state.mainExecutor = null
            }
            controller.unbind()
        }
    }

    LaunchedEffect(controller, torchEnabled) {
        controller.enableTorch(torchEnabled)
    }

    AndroidView(
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                this.controller = controller
                keepScreenOn = true
            }
        },
        modifier = modifier,
    )
}

private fun createController(context: Context): LifecycleCameraController =
    LifecycleCameraController(context).apply {
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
    }
