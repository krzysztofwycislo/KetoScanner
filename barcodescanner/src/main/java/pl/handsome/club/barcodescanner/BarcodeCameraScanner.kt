package pl.handsome.club.barcodescanner

import android.Manifest
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executor


// TODO analyze area overlay
class BarcodeCameraScanner(
    private val fragment: Fragment,
    private val cameraPreviewView: PreviewView,
) {

    private val scannedBarcode = MutableLiveData<String>()

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
            .build()
    )

    private val mainExecutor: Executor by lazy { ContextCompat.getMainExecutor(fragment.requireContext()) }

    private val analyzer: MlKitAnalyzer by lazy {
        MlKitAnalyzer(
            listOf(barcodeScanner),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            mainExecutor
        ) { result ->
            result.getThrowable(barcodeScanner)?.also { Log.e(TAG, "Barcode scanning failed", it) }
            result.getValue(barcodeScanner)?.firstOrNull()?.rawValue?.also(::onScanSuccess)
        }
    }

    private var controller: LifecycleCameraController? = null

    private var isPaused: Boolean = false
    private var isClosed: Boolean = false


    fun getScannedBarcode(): LiveData<String> = scannedBarcode

    @RequiresPermission(Manifest.permission.CAMERA)
    fun start() {
        val aspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY

        controller = LifecycleCameraController(fragment.requireContext()).apply {
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
            setImageAnalysisAnalyzer(mainExecutor, analyzer)
        }.also {
            cameraPreviewView.controller = it
            it.bindToLifecycle(fragment.viewLifecycleOwner)
        }
    }

    private fun onScanSuccess(barcode: String) {
        if (!isPaused) {
            isPaused = true
            controller?.clearImageAnalysisAnalyzer()
            scannedBarcode.value = barcode
        }
    }

    fun resume() {
        val controller = controller ?: return
        if (isPaused && !isClosed) {
            controller.setImageAnalysisAnalyzer(mainExecutor, analyzer)
            isPaused = false
        }
    }

    fun close() {
        if (isClosed) return
        isClosed = true
        controller?.clearImageAnalysisAnalyzer()
        barcodeScanner.close()
    }

    companion object {
        private const val TAG = "BarcodeScanner"
        private val ANALYSIS_TARGET_SIZE = Size(1280, 720)
    }

}
