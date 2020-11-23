package pl.handsome.club.barcodescanner

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


// TODO analyze area overlay
class BarcodeImageAnalyzer(
    private val onScanSuccess: (String) -> Unit,
    private val imageCropPercentages: Pair<Int, Int>
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

//        val widthCropPercent = imageCropPercentages.second
//        val heightCropPercent = imageCropPercentages.first
//        val (widthCrop, heightCrop) = when (targetRotation) {
//            90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
//            else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
//        }

        val cropRect = Rect(0, 0, imageProxy.width, imageProxy.height)
//        cropRect.inset(
//            (mediaImage.width * widthCrop / 2).toInt(),
//            (mediaImage.height * heightCrop / 2).toInt()
//        )

        val finalBitmap = imageProxy.convertYuv420888ImageToBitmap().rotateAndCrop(rotationDegrees, cropRect)
        val image = InputImage.fromBitmap(finalBitmap, 0)

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