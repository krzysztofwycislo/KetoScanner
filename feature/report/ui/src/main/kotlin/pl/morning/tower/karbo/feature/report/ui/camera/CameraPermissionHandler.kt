package pl.morning.tower.karbo.feature.report.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermissionCheck

internal class CameraPermissionHandler(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<String>,
) {

    fun check(): CameraPermissionCheck = activity.cameraPermissionCheck(
        isGranted = ContextCompat.checkSelfPermission(activity, CAMERA) == PackageManager.PERMISSION_GRANTED,
    )

    fun request() {
        launcher.launch(CAMERA)
    }

    fun openSettings() {
        val uri = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }
}

@Composable
internal fun rememberCameraPermissionHandler(onResult: (CameraPermissionCheck) -> Unit): CameraPermissionHandler {
    val activity = checkNotNull(LocalActivity.current)
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        currentOnResult(activity.cameraPermissionCheck(isGranted))
    }
    return remember(activity, launcher) { CameraPermissionHandler(activity, launcher) }
}

private const val CAMERA = Manifest.permission.CAMERA

private fun Activity.cameraPermissionCheck(isGranted: Boolean) = CameraPermissionCheck(
    isGranted = isGranted,
    shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA),
)
