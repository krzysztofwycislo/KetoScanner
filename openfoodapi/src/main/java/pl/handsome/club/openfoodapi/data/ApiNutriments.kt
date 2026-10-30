package pl.handsome.club.openfoodapi.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ApiNutriments(

    @SerialName("energy-kcal_100g") val energyPer100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyPerServing: Double? = null,

    @SerialName("fat_100g") val fatsPer100g: Double? = null,
    @SerialName("fat_serving") val fatsPerServing: Double? = null,

    @SerialName("saturated-fat_100g") val saturatedFatPer100g: Double? = null,
    @SerialName("saturated-fat_serving") val saturatedFatPerServing: Double? = null,

    @SerialName("carbohydrates_100g") val carbohydratesPer100g: Double? = null,
    @SerialName("carbohydrates_serving") val carbohydratesPerServing: Double? = null,

    @SerialName("sugars_100g") val sugarsPer100g: Double? = null,
    @SerialName("sugars_serving") val sugarsPerServing: Double? = null,

    @SerialName("proteins_100g") val proteinsPer100g: Double? = null,
    @SerialName("proteins_serving") val proteinsPerServing: Double? = null,

    @SerialName("salt_100g") val saltPer100g: Double? = null,
    @SerialName("salt_serving") val saltPerServing: Double? = null,

    @SerialName("fiber_100g") val fibrePer100g: Double? = null,
    @SerialName("fiber_serving") val fibrePerServing: Double? = null,

)
