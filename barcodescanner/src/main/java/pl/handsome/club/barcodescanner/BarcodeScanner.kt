package pl.handsome.club.barcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.core.content.ContextCompat


class BarcodeScanner(
    val context: Context
) {

    fun createCameraSource() {

    }

    fun stopScanning() {
        TODO("Not yet implemented")
    }

    fun release() {
        TODO("Not yet implemented")
    }

    fun startScanning() {
        TODO("Not yet implemented")
    }

    companion object {

        private const val TAG = "BARCODE_SCANNER"

    }

}