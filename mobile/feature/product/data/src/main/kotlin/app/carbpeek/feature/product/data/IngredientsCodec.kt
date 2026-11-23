package app.carbpeek.feature.product.data

import app.carbpeek.feature.product.api.model.Ingredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class CachedIngredient(
    val id: String,
    val name: String,
    val label: String? = null,
    val percent: Double? = null,
    val contents: List<CachedIngredient> = emptyList(),
)

private val ingredientsJson = Json { ignoreUnknownKeys = true }

internal fun List<Ingredient>.encodeIngredients(): String = ingredientsJson.encodeToString(map { it.toCached() })

internal fun String.decodeIngredients(): List<Ingredient> =
    ingredientsJson.decodeFromString<List<CachedIngredient>>(this).map { it.toIngredient() }

private fun Ingredient.toCached(): CachedIngredient =
    CachedIngredient(
        id = id,
        name = name,
        label = labelName.takeIf { it != name },
        percent = declaredPercent,
        contents = subIngredients.map { it.toCached() },
    )

private fun CachedIngredient.toIngredient(): Ingredient =
    Ingredient(
        id = id,
        name = name,
        labelName = label ?: name,
        declaredPercent = percent,
        subIngredients = contents.map { it.toIngredient() },
    )
