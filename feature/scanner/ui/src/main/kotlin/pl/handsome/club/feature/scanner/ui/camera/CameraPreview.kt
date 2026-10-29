package pl.handsome.club.feature.scanner.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.handsome.club.barcodescanner.BarcodeCamera

@SuppressLint("MissingPermission")
@Composable
internal fun CameraPreview(isPaused: Boolean, onBarcodeScanned: (String) -> Unit, modifier: Modifier = Modifier) {
    BarcodeCamera(isPaused = isPaused, onBarcodeScanned = onBarcodeScanned, modifier = modifier)
}
