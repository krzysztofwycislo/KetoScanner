package pl.handsome.club.barcodescanner

import android.Manifest
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarcodeScanner(
    private val fragment: Fragment,
    private val cameraPreviewView: PreviewView,
    onScanSuccess: (String) -> Unit
) {

    private lateinit var camera: Camera

    private val barcodeImageAnalyzer = BarcodeImageAnalyzer(onScanSuccess)

    private lateinit var analysisExecutor: ExecutorService
    private lateinit var imageAnalyzer: ImageAnalysis


    @RequiresPermission(Manifest.permission.CAMERA)
    fun setupScanner() {
        val context = fragment.requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        analysisExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener(
            Runnable {
                cameraProviderFuture.get()
                    .apply { unbindAll() }
                    .apply(::bindCamera)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        val metrics = DisplayMetrics().also { cameraPreviewView.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")


        val rotation = cameraPreviewView.display.rotation

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = createPreview(rotation, screenAspectRatio)
        imageAnalyzer = createBarcodeImageAnalyzer(rotation, screenAspectRatio)

        try {
            camera = cameraProvider.bindToLifecycle(fragment, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun createPreview(rotation: Int, screenAspectRatio: Int): Preview {
        return Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .apply { setSurfaceProvider(cameraPreviewView.surfaceProvider) }
    }

    private fun createBarcodeImageAnalyzer(rotation: Int, screenAspectRatio: Int): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .apply { setAnalyzer(analysisExecutor, barcodeImageAnalyzer) }
    }

    fun stop() {
        imageAnalyzer.clearAnalyzer()
    }

    fun close() {
        analysisExecutor.shutdown()
    }

    fun resume() {
        TODO("Not yet implemented")
    }

    fun pause() {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "BarcodeScanner"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

}