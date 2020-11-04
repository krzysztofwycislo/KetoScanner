package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.openfoodapi.data.ApiNutriments
import pl.handsome.club.openfoodapi.data.GetProductResponse


internal fun parseGetProductResponse(response: GetProductResponse): ProductSearchState {
    if (response.apiProduct == null) {
        return ProductSearchState.NotFound
    }

    val apiProduct = response.apiProduct
    return with(apiProduct) {
        val productNutriments = parseProductNutrients(nutriments)

        val product = Product(
            productName,
            response.barcode,
            productNutriments,
            frontImage
        )

        ProductSearchState.Success(product)
    }
}

fun parseProductNutrients(apiNutriments: ApiNutriments): ProductNutriments =
    with(apiNutriments) {
        ProductNutriments(
            energyPer100g,
            energyPerServing,
            fatPer100g
        )
    }
