package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class RealProduct(val product: Product, val rates: Map<DietPreset, DietRate>) {
    val name: String get() = product.name
    val barcode: Barcode get() = Barcode(product.barcode)
}

internal fun food(name: String, nutriments: ProductNutriments, serving: String?): Product =
    createProduct(name, nutriments, serving).copy(barcode = "barcode $name")

internal fun rates(strictKeto: DietRate, keto: DietRate, lowCarb: DietRate) = mapOf(
    DietPreset.STRICT_KETO to strictKeto,
    DietPreset.KETO to keto,
    DietPreset.LOW_CARB to lowCarb,
)

internal fun List<RealProduct>.named(name: String): RealProduct = single { it.name == name }

@Serializable
private class RealProductRow(
    val name: String,
    val category: String,
    val fat: Double,
    val protein: Double,
    val carbs: Double,
    val sugars: Double?,
    val fibre: Double?,
    val serving: String?,
    val strictKeto: DietRate,
    val keto: DietRate,
    val lowCarb: DietRate,
) {
    fun toRealProduct() = RealProduct(
        food(
            name = name,
            nutriments = macroNutriments(
                fatsPer100g = fat,
                proteinsPer100g = protein,
                carbohydratesPer100g = carbs,
                sugarsPer100g = sugars,
                fibrePer100g = fibre,
            ),
            serving = serving,
        ).copy(category = if (category == "drinks") ProductCategory.DRINKS else ProductCategory.OTHER),
        rates(strictKeto = strictKeto, keto = keto, lowCarb = lowCarb),
    )
}

private fun loadRealProducts(): List<RealProduct> {
    val json = checkNotNull(RealProduct::class.java.getResource("/real-products.json")).readText()
    return Json.decodeFromString<List<RealProductRow>>(json).map { it.toRealProduct() }
}

internal val realProducts = loadRealProducts()
