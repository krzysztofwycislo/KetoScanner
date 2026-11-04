package pl.morning.tower.karbo.feature.product.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.product.ui.event.ProductEvent.ProductNavigation

@Serializable
data class ProductRoute(val barcode: String)

fun NavGraphBuilder.productDestination(onNavigation: (ProductNavigation) -> Unit) {
    composable<ProductRoute> {
        ProductScreen(onNavigation)
    }
}
