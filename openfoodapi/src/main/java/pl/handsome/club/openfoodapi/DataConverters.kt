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
        val productNutriments = parseProductNutrients(nutriments ?: ApiNutriments())

        val product = Product(
            productName ?: "",
            response.barcode,
            brand ?: "",
            productNutriments,
            frontImage,
            servingQuantity
        )

        ProductSearchState.Success(product)
    }
}

fun parseProductNutrients(apiNutriments: ApiNutriments): ProductNutriments =
    with(apiNutriments) {
        ProductNutriments(
            energyPerServing = energyPerServing,
            energyPer100g = energyPer100g,
            fatsPerServing = fatsPerServing,
            fatsPer100g = fatsPer100g,
            proteinsPerServing = proteinsPerServing,
            proteinsPer100g = proteinsPer100g,
            carbohydratesPerServing = carbohydratesPerServing,
            carbohydratesPer100g = carbohydratesPer100g,
            saltPerServing = saltPerServing,
            saltPer100g = saltPer100g
        )
    }
