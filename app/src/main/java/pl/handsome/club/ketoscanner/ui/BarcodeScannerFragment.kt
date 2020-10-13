package pl.handsome.club.ketoscanner.ui

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.barcode_scanner_fragment.*
import pl.handsome.club.barcodescanner.BarcodeImageAnalyzer
import pl.handsome.club.ketoscanner.R
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class BarcodeScannerFragment : Fragment(R.layout.barcode_scanner_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasCameraPermission())
            onCameraPermissionGranted()
        else
            onCameraPermissionDenied()
    }

    private fun onCameraPermissionDenied() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(requireActivity(), permissions, CAMERA_PERMISSION_RC)
    }

    private fun hasCameraPermission(): Boolean {
        val rc = checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return rc == PackageManager.PERMISSION_GRANTED
    }

    private fun onCameraPermissionGranted() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            val cameraProvider = cameraProviderFuture.get()

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { cameraPreviewView.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

            val rotation = cameraPreviewView.display.rotation

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
//                .setTargetRotation(rotation)
                .build()

            val barcodeAnalyzer = BarcodeImageAnalyzer {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }

            imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyzer)

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != CAMERA_PERMISSION_RC || grantResults.isEmpty()) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            onCameraPermissionGranted()
        } else {
            findNavController().navigateUp()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}