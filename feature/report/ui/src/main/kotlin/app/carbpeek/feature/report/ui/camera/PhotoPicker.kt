package app.carbpeek.feature.report.ui.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

internal class PhotoPicker(private val launcher: ActivityResultLauncher<PickVisualMediaRequest>) {

    fun launch() {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
internal fun rememberPhotoPicker(onPicked: (String) -> Unit): PhotoPicker {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { currentOnPicked(it.toString()) }
    }
    return remember(launcher) { PhotoPicker(launcher) }
}
