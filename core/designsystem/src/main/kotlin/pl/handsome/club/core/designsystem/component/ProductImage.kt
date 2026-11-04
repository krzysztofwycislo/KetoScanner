package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.ProductImage(
    url: String?,
    contentDescription: UiText?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholderColor = AppTheme.colors.placeholder
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }
    AsyncImage(
        model = url,
        contentDescription = contentDescription?.resolve(),
        modifier = modifier,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = contentScale,
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductImageNoUrlPreview() {
    AppTheme {
        Ui.ProductImage(
            url = null,
            contentDescription = UiText.Res(R.string.designsystem_preview_image),
            modifier = Modifier.size(AppTheme.sizes.productPhoto),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductImageWithUrlPreview() {
    AppTheme {
        Ui.ProductImage(
            url = "https://images.openfoodfacts.org/images/products/sample.jpg",
            contentDescription = UiText.Res(R.string.designsystem_preview_image),
            modifier = Modifier.size(AppTheme.sizes.productPhoto),
        )
    }
}
