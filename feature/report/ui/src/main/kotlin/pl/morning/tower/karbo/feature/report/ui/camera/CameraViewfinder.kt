package pl.morning.tower.karbo.feature.report.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.morning.tower.karbo.photocamera.PhotoCamera
import pl.morning.tower.karbo.photocamera.PhotoCameraState

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
