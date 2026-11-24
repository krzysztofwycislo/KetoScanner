package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.ImageLoadStatus
import app.carbpeek.core.designsystem.component.RetryingImage
import app.carbpeek.core.designsystem.component.RetryingImageState
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.currentLoad
import app.carbpeek.core.designsystem.component.rememberRetryingImageState
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnImageClicked
import app.carbpeek.feature.product.ui.state.ProductHeaderState

@Composable
internal fun ProductPhoto(
    header: ProductHeaderState,
    photo: RetryingImageState,
    retriesAutomatically: Boolean,
    onAction: (ProductAction) -> Unit,
) {
    val url = header.imageUrl
    val clickModifier = if (header.isImageClickable) {
        Modifier.clickable { onAction(OnImageClicked) }
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.productPhoto)
            .clip(AppTheme.shapes.large)
            .background(AppTheme.colors.background.surface)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.large)
            .then(clickModifier),
    ) {
        if (url != null) {
            Ui.RetryingImage(
                url = url,
                state = photo,
                contentDescription = header.imageDescription,
                retriesAutomatically = retriesAutomatically,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (url == null || photo.currentLoad().status != ImageLoadStatus.LOADED) {
            PhotoPlaceholder(header.placeholderIcon)
        }
    }
}

@Composable
private fun PhotoPlaceholder(icon: AppIcon) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.placeholder),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            tint = AppTheme.colors.text.subtle,
            modifier = Modifier.size(AppTheme.sizes.icon.xl),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductPhotoWithoutUrlPreview() {
    AppTheme {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            listOf(AppIcons.Jar, AppIcons.Bag).forEach { icon ->
                ProductPhoto(
                    header = ProductHeaderState.example(placeholderIcon = icon),
                    photo = rememberRetryingImageState(url = null),
                    retriesAutomatically = false,
                    onAction = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoPlaceholderPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .padding(AppTheme.spacing.l)
                .size(AppTheme.sizes.productPhoto),
        ) {
            PhotoPlaceholder(AppIcons.Jar)
        }
    }
}
