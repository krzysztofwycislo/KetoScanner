package pl.handsome.club.ketoscanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.barcode_scanner_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pl.handsome.club.barcodescanner.BarcodeScanner
import pl.handsome.club.ketoscanner.R


class BarcodeScannerFragment : Fragment(R.layout.barcode_scanner_fragment) {

    private var barcodeScanner: BarcodeScanner? = null


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
        CoroutineScope(Dispatchers.IO).launch {
            val previewConfig = PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build()

            val preview = Preview(previewConfig)
            preview.setOnPreviewOutputUpdateListener { previewOutput ->
                cameraPreview.surfaceTexture = previewOutput.surfaceTexture
            }

            CameraX.bindToLifecycle(this@BarcodeScannerFragment, preview)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != CAMERA_PERMISSION_RC) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeScanner?.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeScanner?.release()
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
        private const val TAG = "BarcodeScannerFragment"
    }
}