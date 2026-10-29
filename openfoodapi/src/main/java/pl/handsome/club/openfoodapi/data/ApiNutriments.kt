package pl.handsome.club.openfoodapi.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ApiNutriments(

	@Json(name = "energy-kcal_100g") val energyPer100g: Double? = null,
	@Json(name = "energy-kcal_serving") val energyPerServing: Double? = null,

	@Json(name = "fat_100g") val fatsPer100g: Double? = null,
	@Json(name = "fat_serving") val fatsPerServing: Double? = null,

	@Json(name = "saturated-fat_100g") val saturatedFatPer100g: Double? = null,
	@Json(name = "saturated-fat_serving") val saturatedFatPerServing: Double? = null,

	@Json(name = "carbohydrates_100g") val carbohydratesPer100g: Double? = null,
	@Json(name = "carbohydrates_serving") val carbohydratesPerServing: Double? = null,

	@Json(name = "sugars_100g") val sugarsPer100g: Double? = null,
	@Json(name = "sugars_serving") val sugarsPerServing: Double? = null,

	@Json(name = "proteins_100g") val proteinsPer100g: Double? = null,
	@Json(name = "proteins_serving") val proteinsPerServing: Double? = null,

	@Json(name = "salt_100g") val saltPer100g: Double? = null,
	@Json(name = "salt_serving") val saltPerServing: Double? = null,

	@Json(name = "fiber_100g") val fibrePer100g: Double? = null,
	@Json(name = "fiber_serving") val fibrePerServing: Double? = null,

)