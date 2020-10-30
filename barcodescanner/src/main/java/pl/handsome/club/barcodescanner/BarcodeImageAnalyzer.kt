package pl.handsome.club.barcodescanner

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


// TODO fix image rotation issue
// TODO analyze smaller spot
class BarcodeImageAnalyzer(
    private val onScanSuccess: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

//        val imageBitmap = imageProxy.toBitmap()
//        val resizedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.width, imageBitmap.height/2)
//        val rotation = imageProxy.imageInfo.rotationDegrees
//        val image = InputImage.fromBitmap(resizedBitmap, imageProxy.imageInfo.rotationDegrees)
//        Log.d(ContentValues.TAG, "image: ${image.width}, ${image.height}")

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//        Log.d(ContentValues.TAG, "mediaImage: ${mediaImage.width}, ${mediaImage.height}, $rotation")

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
            ?.let { onScanSuccess(it) }
    }

    companion object {
        const val TAG = "BarcodeImageAnalyzer"
    }

}