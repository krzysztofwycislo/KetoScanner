package app.carbpeek.feature.product.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation
import kotlinx.serialization.Serializable

@Serializable
data class ProductRoute(val barcode: String)

fun NavGraphBuilder.productDestination(onNavigation: (ProductNavigation) -> Unit) {
    composable<ProductRoute> {
        ProductScreen(onNavigation)
    }
}
