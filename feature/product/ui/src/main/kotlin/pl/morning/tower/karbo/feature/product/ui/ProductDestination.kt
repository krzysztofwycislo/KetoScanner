package pl.handsome.club.feature.product.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation

@Serializable
data class ProductRoute(val barcode: String)

fun NavGraphBuilder.productDestination(onNavigation: (ProductNavigation) -> Unit) {
    composable<ProductRoute> {
        ProductScreen(onNavigation)
    }
}
