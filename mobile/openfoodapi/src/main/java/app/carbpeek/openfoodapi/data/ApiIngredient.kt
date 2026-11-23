package app.carbpeek.openfoodapi.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiIngredient(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("percent") val percent: String? = null,
    @SerialName("is_in_taxonomy") val isInTaxonomy: Int? = null,
    @SerialName("ingredients") val ingredients: List<ApiIngredient>? = null,
)
