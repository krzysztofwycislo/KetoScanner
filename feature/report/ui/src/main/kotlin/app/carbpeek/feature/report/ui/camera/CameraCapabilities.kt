package app.carbpeek.feature.report.ui.camera

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun rememberHasTorch(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) }
}
