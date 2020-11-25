package pl.handsome.club.openfoodapi.data

import com.squareup.moshi.Json


data class ApiNutriments(

	@Json(name = "energy-kcal_100g") val energyPer100g: Double?,
	@Json(name = "energy-kcal_serving") val energyPerServing: Double?,

	@Json(name = "fat_100g") val fatsPer100g: Double?,
	@Json(name = "fat_serving") val fatsPerServing: Double?,

	@Json(name = "saturated-fat_100g") val saturatedFatPer100g: Double?,
	@Json(name = "saturated-fat_serving") val saturatedFatPerServing: Double?,

	@Json(name = "carbohydrates_100g") val carbohydratesPer100g: Double?,
	@Json(name = "carbohydrates_serving") val carbohydratesPerServing: Double?,

	@Json(name = "sugars_100g") val sugarsPer100g: Double?,
	@Json(name = "sugars_serving") val sugarsPerServing: Double?,

	@Json(name = "proteins_100g") val proteinsPer100g: Double?,
	@Json(name = "proteins_serving") val proteinsPerServing: Double?,

	@Json(name = "salt_100g") val saltPer100g: Double?,
	@Json(name = "salt_serving") val saltPerServing: Double?,

)