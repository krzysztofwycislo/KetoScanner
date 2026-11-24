package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.CircleIconButton
import app.carbpeek.core.designsystem.component.InfoChip
import app.carbpeek.core.designsystem.component.RetryingImageState
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.rememberRetryingImageState
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnBackClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnFavouriteClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnNameClicked
import app.carbpeek.feature.product.ui.state.FavouriteButtonState
import app.carbpeek.feature.product.ui.state.ProductHeaderState

@Composable
internal fun Header(
    favourite: FavouriteButtonState?,
    onAction: (ProductAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.CircleIconButton(
            icon = AppIcons.Back.imageVector,
            contentDescription = UiText.Res(R.string.product_back),
            onClick = { onAction(OnBackClicked) },
        )
        if (favourite != null) {
            Ui.CircleIconButton(
                icon = favourite.icon.imageVector,
                contentDescription = favourite.description,
                onClick = { onAction(OnFavouriteClicked) },
                enabled = favourite.isEnabled,
                contentColor = if (favourite.isFavourite) AppTheme.colors.favourite else AppTheme.colors.text.default,
            )
        }
    }
}

@Composable
internal fun ProductBlock(
    header: ProductHeaderState,
    photo: RetryingImageState,
    retriesAutomatically: Boolean,
    onAction: (ProductAction) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductPhoto(
            header = header,
            photo = photo,
            retriesAutomatically = retriesAutomatically,
            onAction = onAction,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            header.category?.let {
                Ui.InfoChip(text = it.label.resolve(), icon = it.icon.imageVector)
            }
            Text(
                text = header.name.resolve(),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.text.default,
                maxLines = header.nameMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onAction(OnNameClicked) },
            )
            header.brand?.let {
                Text(text = it.resolve(), style = AppTheme.typography.body, color = AppTheme.colors.text.muted)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductHeaderPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            Header(FavouriteButtonState.exampleFavourite, {})
            ProductBlock(
                header = ProductHeaderState.example(),
                photo = rememberRetryingImageState(url = null),
                retriesAutomatically = false,
                onAction = {},
            )
        }
    }
}
