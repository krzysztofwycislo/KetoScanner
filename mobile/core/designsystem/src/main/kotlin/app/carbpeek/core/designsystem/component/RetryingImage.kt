package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import coil3.compose.AsyncImage

private val RetryingImageStateSaver = Saver<RetryingImageState, Int>(
    save = { it.load.value.automaticRetries },
    restore = { RetryingImageState(automaticRetries = it) },
)

@Composable
fun rememberRetryingImageState(url: String?): RetryingImageState =
    rememberSaveable(url, saver = RetryingImageStateSaver) {
        RetryingImageState()
    }

@Composable
fun RetryingImageState.currentLoad(): ImageLoad {
    val currentLoad by key(this) {
        load.collectAsStateWithLifecycle()
    }
    return currentLoad
}

@Composable
fun Ui.RetryingImage(
    url: String,
    state: RetryingImageState,
    contentDescription: UiText?,
    retriesAutomatically: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val attempt = state.currentLoad().attempt
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(state, lifecycleOwner, retriesAutomatically) {
        if (retriesAutomatically) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                state.retryWhileActive()
            }
        }
    }
    key(state, attempt) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription?.resolve(),
            modifier = modifier,
            onSuccess = { state.onLoaded(attempt) },
            onError = { state.onFailed(attempt = attempt, error = it.result.throwable) },
            contentScale = contentScale,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetryingImagePreview() {
    AppTheme {
        val url = "https://images.openfoodfacts.org/images/products/sample.jpg"
        Ui.RetryingImage(
            url = url,
            state = rememberRetryingImageState(url),
            contentDescription = UiText.Res(R.string.designsystem_preview_image),
            retriesAutomatically = false,
            modifier = Modifier.size(AppTheme.sizes.productPhoto),
        )
    }
}
