package pl.handsome.club.barcodescanner

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


class BarcodeImageAnalyzer(
    private val onBarcodeFound: (String) -> Unit
) : ImageAnalysis.Analyzer {


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        Log.d(ContentValues.TAG, "mediaImage: ${mediaImage.width}, ${mediaImage.height}")

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
            .build()

        BarcodeScanning.getClient(options)
            .process(image)
            .addOnSuccessListener(::onSuccess)
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun onSuccess(barcodes: List<Barcode>) {
        barcodes.firstOrNull()
            ?.rawValue
            ?.apply { Log.d(TAG, this) }
            ?.let { onBarcodeFound(it) }
    }

    companion object {
        const val TAG = "BarcodeImageAnalyzer"
    }

}