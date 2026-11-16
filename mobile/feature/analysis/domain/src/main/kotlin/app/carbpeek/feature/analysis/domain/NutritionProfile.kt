package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.usableServingGrams

internal data class NutritionProfile(
    val carbs: Double,
    val fat: Double,
    val protein: Double,
    val sugars: Double?,
    val servingGrams: Double?,
    val fibre: Double?,
    val liquid: Boolean,
) {

    val energy: Double = FAT_KCAL_PER_GRAM * fat + PROTEIN_KCAL_PER_GRAM * protein + CARBS_KCAL_PER_GRAM * carbs

    val carbEnergyShare: Double = if (energy > 0) CARBS_KCAL_PER_GRAM * carbs / energy else 0.0

    val fatAndProteinToCarbRatio: Double = if (carbs > 0) (fat + protein) / carbs else Double.POSITIVE_INFINITY

    val carbsPerServing: Double? = servingGrams?.let { carbs * it / 100 }

    private companion object {
        const val FAT_KCAL_PER_GRAM = 9
        const val PROTEIN_KCAL_PER_GRAM = 4
        const val CARBS_KCAL_PER_GRAM = 4
    }
}

private val LIQUID_CATEGORIES = setOf(ProductCategory.DRINKS, ProductCategory.MILK_AND_JUICE, ProductCategory.WATER)

internal fun Product.toNutritionProfile(): NutritionProfile? {
    val carbs = nutriments.carbohydratesPer100g ?: return null
    val fat = nutriments.fatsPer100g ?: return null
    val protein = nutriments.proteinsPer100g ?: return null
    return NutritionProfile(
        carbs = carbs,
        fat = fat,
        protein = protein,
        sugars = nutriments.sugarsPer100g,
        servingGrams = usableServingGrams,
        fibre = nutriments.fibrePer100g,
        liquid = category in LIQUID_CATEGORIES,
    )
}
