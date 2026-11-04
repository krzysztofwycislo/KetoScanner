package pl.handsome.club.barcodescanner

import android.Manifest
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

private const val TAG = "BarcodeCamera"
private val ANALYSIS_TARGET_SIZE = Size(1280, 720)

@RequiresPermission(Manifest.permission.CAMERA)
@Composable
fun BarcodeCamera(
    isPaused: Boolean,
    torchEnabled: Boolean,
    scanArea: Rect?,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier)
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember(context) { createController(context) }
    val currentIsPaused by rememberUpdatedState(isPaused)
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)
    val currentTorchEnabled by rememberUpdatedState(torchEnabled)
    val currentScanArea by rememberUpdatedState(scanArea)
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(controller, lifecycleOwner) {
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
                .build()
        )
        val analyzer = MlKitAnalyzer(
            listOf(scanner),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(context),
        ) { result ->
            result.getThrowable(scanner)?.also { Log.e(TAG, "Barcode scanning failed", it) }
            val barcode = result.getValue(scanner)
                ?.firstOrNull { it.rawValue != null && it.isInside(currentScanArea, cameraOffset) }
                ?.rawValue
            if (barcode != null && !currentIsPaused) currentOnBarcodeScanned(barcode)
        }
        controller.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        controller.bindToLifecycle(lifecycleOwner)

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
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            disposed = true
            observedCameraState?.removeObserver(torchObserver)
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            scanner.close()
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
        modifier = modifier.onGloballyPositioned { cameraOffset = it.boundsInWindow().topLeft },
    )
}

private fun Barcode.isInside(scanArea: Rect?, cameraOffset: Offset): Boolean {
    if (scanArea == null) return true
    val bounds = boundingBox?.toComposeRect()?.translate(cameraOffset) ?: return false
    return scanArea.contains(bounds.topLeft) && scanArea.contains(bounds.bottomRight)
}

private fun createController(context: Context): LifecycleCameraController {
    val aspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
    return LifecycleCameraController(context).apply {
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        previewResolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(aspectRatioStrategy)
            .build()
        imageAnalysisResolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(aspectRatioStrategy)
            .setResolutionStrategy(
                ResolutionStrategy(
                    ANALYSIS_TARGET_SIZE,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()
    }
}
