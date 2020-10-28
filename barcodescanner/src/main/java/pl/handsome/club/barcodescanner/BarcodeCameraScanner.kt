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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BarcodeCameraScanner(
    private val fragment: Fragment,
    private val cameraPreviewView: PreviewView,
) {

    private val scannedBarcode = MutableLiveData<String>()

    private lateinit var camera: Camera
    private lateinit var preview: Preview

    private lateinit var analysisExecutor: ExecutorService
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var barcodeImageAnalyzer: BarcodeImageAnalyzer


    private var isPaused: Boolean = false


    fun getScannedBarcode(): LiveData<String> = scannedBarcode

    @RequiresPermission(Manifest.permission.CAMERA)
    fun start() {
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
        preview = createPreview(rotation, screenAspectRatio)

        barcodeImageAnalyzer = BarcodeImageAnalyzer(::onScanSuccess)
        imageAnalyzer = createBarcodeImageAnalyzer(rotation, screenAspectRatio)

        try {
            camera = cameraProvider.bindToLifecycle(
                fragment,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
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

    private fun onScanSuccess(barcode: String) {
        if (!isPaused) {
            pause()
            scannedBarcode.value = barcode
        }
    }

    private fun pause() {
        isPaused = true
        preview.setSurfaceProvider(null)
        imageAnalyzer.clearAnalyzer()
    }

    fun resume() {
        preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)
        imageAnalyzer.setAnalyzer(analysisExecutor, barcodeImageAnalyzer)
        isPaused = false
    }

    fun close() {
        analysisExecutor.shutdown()
    }

    companion object {
        private const val TAG = "BarcodeScanner"
    }

}