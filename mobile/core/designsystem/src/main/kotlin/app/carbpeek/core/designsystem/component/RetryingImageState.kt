package app.carbpeek.core.designsystem.component

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import coil3.network.HttpException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

private val AUTOMATIC_RETRY_PAUSES = listOf(5.seconds, 15.seconds, 45.seconds)
private val MISSING_IMAGE_CODES = setOf(404, 410)

enum class ImageLoadStatus {
    LOADING,
    LOADED,
    FAILED,
    MISSING,
}

@Immutable
data class ImageLoad(
    val attempt: Int = 0,
    val automaticRetries: Int = 0,
    val status: ImageLoadStatus = ImageLoadStatus.LOADING,
)

@Stable
class RetryingImageState(automaticRetries: Int = 0) {

    private val mutableLoad = MutableStateFlow(ImageLoad(automaticRetries = automaticRetries))
    val load: StateFlow<ImageLoad> = mutableLoad.asStateFlow()

    fun retry() {
        mutableLoad.update { load ->
            if (load.status == ImageLoadStatus.FAILED) {
                load.nextAttempt()
            } else {
                load
            }
        }
    }

    fun reload() {
        mutableLoad.update { load ->
            if (load.status == ImageLoadStatus.LOADED) {
                load
            } else {
                load.nextAttempt()
            }
        }
    }

    internal fun onLoaded(attempt: Int) {
        onFinished(attempt = attempt, status = ImageLoadStatus.LOADED)
    }

    internal fun onFailed(attempt: Int, error: Throwable) {
        onFinished(attempt = attempt, status = imageFailureStatusOf(error))
    }

    internal suspend fun retryWhileActive() {
        retry()
        mutableLoad.collectLatest { load ->
            val pause = AUTOMATIC_RETRY_PAUSES.getOrNull(load.automaticRetries)
            if (load.status == ImageLoadStatus.FAILED && pause != null) {
                delay(pause)
                retryAutomatically(failedLoad = load)
            }
        }
    }

    private fun retryAutomatically(failedLoad: ImageLoad) {
        mutableLoad.update { load ->
            if (load == failedLoad) {
                load.nextAttempt().copy(automaticRetries = load.automaticRetries + 1)
            } else {
                load
            }
        }
    }

    private fun onFinished(attempt: Int, status: ImageLoadStatus) {
        mutableLoad.update { load ->
            if (load.attempt == attempt) {
                load.copy(status = status)
            } else {
                load
            }
        }
    }

    private fun ImageLoad.nextAttempt(): ImageLoad = copy(attempt = attempt + 1, status = ImageLoadStatus.LOADING)
}

internal fun imageFailureStatusOf(error: Throwable): ImageLoadStatus =
    if (error is HttpException && error.response.code in MISSING_IMAGE_CODES) {
        ImageLoadStatus.MISSING
    } else {
        ImageLoadStatus.FAILED
    }
