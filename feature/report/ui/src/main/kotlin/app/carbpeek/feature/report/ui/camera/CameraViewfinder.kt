package app.carbpeek.feature.report.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.carbpeek.photocamera.PhotoCamera
import app.carbpeek.photocamera.PhotoCameraState

@SuppressLint("MissingPermission")
@Composable
internal fun CameraViewfinder(
    camera: PhotoCameraState,
    torchEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    PhotoCamera(
        state = camera,
        torchEnabled = torchEnabled,
        modifier = modifier,
    )
}
