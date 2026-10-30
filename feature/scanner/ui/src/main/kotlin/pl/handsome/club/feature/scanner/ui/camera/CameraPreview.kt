package pl.handsome.club.feature.scanner.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.handsome.club.barcodescanner.BarcodeCamera

@SuppressLint("MissingPermission")
@Composable
internal fun CameraPreview(
    isPaused: Boolean,
    torchEnabled: Boolean,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BarcodeCamera(
        isPaused = isPaused,
        torchEnabled = torchEnabled,
        onBarcodeScanned = onBarcodeScanned,
        modifier = modifier,
    )
}
