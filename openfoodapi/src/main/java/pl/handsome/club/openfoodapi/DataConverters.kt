package pl.handsome.club.openfoodapi

import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.openfoodapi.data.ApiNutriments
import pl.handsome.club.openfoodapi.data.GetProductResponse


internal fun parseGetProductResponse(response: GetProductResponse): Product? {
    val apiProduct = response.apiProduct ?: return null

    return with(apiProduct) {
        val productNutriments = parseProductNutrients(nutriments ?: ApiNutriments())

        Product(
            productName ?: "",
            response.barcode,
            brand ?: "",
            productNutriments,
            frontImage,
            servingQuantity
        )
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
