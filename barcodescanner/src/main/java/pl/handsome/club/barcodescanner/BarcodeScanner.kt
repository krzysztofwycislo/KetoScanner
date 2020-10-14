package pl.handsome.club.barcodescanner

import android.Manifest
import android.content.ContentValues
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class BarcodeScanner(
    private val fragment: Fragment,
    private val cameraPreviewView: PreviewView,
    onScanSuccess: (String) -> Unit
) {

    private val barcodeImageAnalyzer = BarcodeImageAnalyzer(onScanSuccess)

    @RequiresPermission(Manifest.permission.CAMERA)
    fun setupScanner() {
        val context = fragment.requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            Runnable { cameraProviderFuture.get().apply(::setupCameraScanner) },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun setupCameraScanner(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)

        val imageAnalyzer = createBarcodeImageAnalyzer()

        try {
            cameraProvider.bindToLifecycle(fragment, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
            Log.e(ContentValues.TAG, "Use case binding failed", exc)
        }
    }

    private fun createBarcodeImageAnalyzer(): ImageAnalysis {
        val imageAnalyzer = ImageAnalysis.Builder().build()
        imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeImageAnalyzer)

        return imageAnalyzer
    }

}