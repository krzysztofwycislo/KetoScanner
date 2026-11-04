package pl.morning.tower.karbo.feature.product.data

import pl.morning.tower.karbo.core.database.product.ProductEntity
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
import kotlin.time.Instant

internal fun ProductEntity.toDomain(): Product =
    Product(
        name = name,
        barcode = barcode,
        brand = brand,
        nutriments = ProductNutriments(
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
        ),
        imageUrl = imageUrl,
        servingAmount = servingAmount,
        category = ProductCategory.fromName(category),
    )

internal fun Product.toEntity(fetchedAt: Instant): ProductEntity =
    ProductEntity(
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        servingAmount = servingAmount,
        category = category.name,
        energyPerServing = nutriments.energyPerServing,
        energyPer100g = nutriments.energyPer100g,
        fatsPerServing = nutriments.fatsPerServing,
        fatsPer100g = nutriments.fatsPer100g,
        proteinsPerServing = nutriments.proteinsPerServing,
        proteinsPer100g = nutriments.proteinsPer100g,
        carbohydratesPerServing = nutriments.carbohydratesPerServing,
        carbohydratesPer100g = nutriments.carbohydratesPer100g,
        saltPerServing = nutriments.saltPerServing,
        saltPer100g = nutriments.saltPer100g,
        sugarsPerServing = nutriments.sugarsPerServing,
        sugarsPer100g = nutriments.sugarsPer100g,
        fibrePerServing = nutriments.fibrePerServing,
        fibrePer100g = nutriments.fibrePer100g,
        fetchedAt = fetchedAt,
    )
