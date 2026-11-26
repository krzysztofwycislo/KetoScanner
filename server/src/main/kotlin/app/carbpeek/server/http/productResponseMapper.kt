package app.carbpeek.server.http

import app.carbpeek.server.product.Ingredient
import app.carbpeek.server.product.NutriScore
import app.carbpeek.server.product.Nutrients
import app.carbpeek.server.product.Product
import app.carbpeek.server.product.ProductCategory
import app.carbpeek.shared.contract.NutrientAmounts
import app.carbpeek.shared.contract.ProductCategoryCode
import app.carbpeek.shared.contract.ProductIngredient
import app.carbpeek.shared.contract.ProductNutriScore
import app.carbpeek.shared.contract.ProductNutrients
import app.carbpeek.shared.contract.ProductResponse

internal fun Product.toResponse(): ProductResponse =
    ProductResponse(
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        servingQuantity = servingQuantity,
        category = category.toCode(),
        labelLanguage = labelLanguage,
        ingredientsText = ingredientsText,
        ingredients = ingredients?.map(Ingredient::toResponse),
        nutriScore = nutriScore?.toResponse(),
        novaGroup = novaGroup,
        nutrients = ProductNutrients(
            per100g = nutrientsPer100g.toResponse(),
            perServing = nutrientsPerServing.toResponse(),
        ),
    )

private fun Nutrients.toResponse(): NutrientAmounts =
    NutrientAmounts(
        energyKcal = energyKcal,
        fat = fat,
        saturatedFat = saturatedFat,
        carbohydrates = carbohydrates,
        sugars = sugars,
        fibre = fibre,
        proteins = proteins,
        salt = salt,
    )

private fun Ingredient.toResponse(): ProductIngredient =
    ProductIngredient(
        id = id,
        name = name,
        labelName = labelName,
        declaredPercent = declaredPercent,
        subIngredients = subIngredients.map(Ingredient::toResponse),
    )

private fun NutriScore.toResponse(): ProductNutriScore =
    when (this) {
        NutriScore.A -> ProductNutriScore.A
        NutriScore.B -> ProductNutriScore.B
        NutriScore.C -> ProductNutriScore.C
        NutriScore.D -> ProductNutriScore.D
        NutriScore.E -> ProductNutriScore.E
    }

private fun ProductCategory.toCode(): ProductCategoryCode =
    when (this) {
        ProductCategory.SPREADS -> ProductCategoryCode.SPREADS
        ProductCategory.OILS_AND_SAUCES -> ProductCategoryCode.OILS_AND_SAUCES
        ProductCategory.WATER -> ProductCategoryCode.WATER
        ProductCategory.DRINKS -> ProductCategoryCode.DRINKS
        ProductCategory.MILK_AND_JUICE -> ProductCategoryCode.MILK_AND_JUICE
        ProductCategory.YOGURT_AND_CREAM -> ProductCategoryCode.YOGURT_AND_CREAM
        ProductCategory.CHOCOLATE_AND_BARS -> ProductCategoryCode.CHOCOLATE_AND_BARS
        ProductCategory.CHEESE -> ProductCategoryCode.CHEESE
        ProductCategory.EGGS -> ProductCategoryCode.EGGS
        ProductCategory.FISH -> ProductCategoryCode.FISH
        ProductCategory.NUTS_AND_SEEDS -> ProductCategoryCode.NUTS_AND_SEEDS
        ProductCategory.GRAINS -> ProductCategoryCode.GRAINS
        ProductCategory.BISCUITS -> ProductCategoryCode.BISCUITS
        ProductCategory.TEA_AND_GREENS -> ProductCategoryCode.TEA_AND_GREENS
        ProductCategory.MEAT -> ProductCategoryCode.MEAT
        ProductCategory.OTHER -> ProductCategoryCode.OTHER
    }
