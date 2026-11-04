package pl.morning.tower.karbo.openfoodapi

import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
import pl.morning.tower.karbo.openfoodapi.data.ApiNutriments
import pl.morning.tower.karbo.openfoodapi.data.GetProductResponse


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
            servingQuantity,
            productCategoryOf(categoriesTags)
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
            saltPer100g = saltPer100g,
            sugarsPerServing = sugarsPerServing,
            sugarsPer100g = sugarsPer100g,
            fibrePerServing = fibrePerServing,
            fibrePer100g = fibrePer100g,
        )
    }
