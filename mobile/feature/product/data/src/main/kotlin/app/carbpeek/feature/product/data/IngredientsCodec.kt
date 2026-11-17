package app.carbpeek.feature.product.data

import app.carbpeek.feature.product.api.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class CachedIngredient(
    val id: String,
    val name: String,
    val percent: Double? = null,
    val sub: List<CachedIngredient> = emptyList(),
)

private val ingredientsJson = Json { ignoreUnknownKeys = true }

internal fun List<Ingredient>.encodeIngredients(): String = ingredientsJson.encodeToString(map { it.toCached() })

internal fun String.decodeIngredients(): List<Ingredient> =
    ingredientsJson.decodeFromString<List<CachedIngredient>>(this).map { it.toIngredient() }

private fun Ingredient.toCached(): CachedIngredient =
    CachedIngredient(
        id = id,
        name = name,
        percent = declaredPercent,
        sub = subIngredients.map { it.toCached() },
    )

private fun CachedIngredient.toIngredient(): Ingredient =
    Ingredient(
        id = id,
        name = name,
        declaredPercent = percent,
        subIngredients = sub.map { it.toIngredient() },
    )
