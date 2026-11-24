package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import app.carbpeek.core.designsystem.component.ImageLoadStatus
import app.carbpeek.core.designsystem.component.RetryingImage
import app.carbpeek.core.designsystem.component.RetryingImageState
import app.carbpeek.core.designsystem.component.TextButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.currentLoad
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import app.carbpeek.feature.product.ui.state.ImagePreviewState

@Composable
internal fun ImagePreview(
    state: ImagePreviewState,
    onLoaded: () -> Unit,
    onAction: (ProductAction) -> Unit,
) {
    val photo = remember(state.url) { RetryingImageState() }
    val status = photo.currentLoad().status
    LaunchedEffect(photo, status) {
        if (status == ImageLoadStatus.LOADED) {
            onLoaded()
        }
    }
    Dialog(onDismissRequest = { onAction(OnImagePreviewDismissed) }) {
        ImagePreviewPanel(status = status, onRetryClicked = photo::retry) { modifier ->
            Ui.RetryingImage(
                url = state.url,
                state = photo,
                contentDescription = state.description.takeIf { status == ImageLoadStatus.LOADED },
                retriesAutomatically = false,
                modifier = modifier,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ImagePreviewPanel(
    status: ImageLoadStatus,
    onRetryClicked: () -> Unit,
    photo: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints {
        val isLoaded = status == ImageLoadStatus.LOADED
        val panelModifier = if (isLoaded) {
            Modifier
        } else {
            Modifier.heightIn(min = maxWidth)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background.default, AppTheme.shapes.small)
                .then(panelModifier),
            contentAlignment = Alignment.Center,
        ) {
            photo(if (isLoaded) Modifier.fillMaxWidth() else Modifier.matchParentSize())
            when (status) {
                ImageLoadStatus.LOADING -> LoadingIndicator()
                ImageLoadStatus.LOADED -> Unit
                ImageLoadStatus.FAILED -> FailureMessage(
                    message = stringResource(R.string.product_image_failed),
                    onRetryClicked = onRetryClicked,
                )
                ImageLoadStatus.MISSING -> FailureMessage(
                    message = stringResource(R.string.product_image_missing),
                    onRetryClicked = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val description = stringResource(R.string.product_image_loading)
    CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
}

@Composable
private fun FailureMessage(message: String, onRetryClicked: (() -> Unit)?) {
    Column(
        modifier = Modifier.padding(horizontal = AppTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = message,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (onRetryClicked != null) {
            Ui.TextButton(
                text = stringResource(R.string.product_retry),
                onClick = onRetryClicked,
                color = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun ImagePreviewPanelExample(status: ImageLoadStatus) {
    AppTheme {
        ImagePreviewPanel(status = status, onRetryClicked = {}) { modifier ->
            Box(modifier = modifier)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun ImagePreviewLoadingPreview() {
    ImagePreviewPanelExample(ImageLoadStatus.LOADING)
}

@Preview(widthDp = 360)
@Composable
private fun ImagePreviewFailedPreview() {
    ImagePreviewPanelExample(ImageLoadStatus.FAILED)
}

@Preview(widthDp = 360)
@Composable
private fun ImagePreviewMissingPreview() {
    ImagePreviewPanelExample(ImageLoadStatus.MISSING)
}

@Preview(widthDp = 360, fontScale = 1.5f)
@Composable
private fun ImagePreviewFailedLargeFontPreview() {
    ImagePreviewPanelExample(ImageLoadStatus.FAILED)
}
