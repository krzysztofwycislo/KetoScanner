package pl.handsome.club.openfoodapi.data

import com.squareup.moshi.Json

// #IDEA nova group can be added
data class ApiNutriments(
	@Json(name = "energy") val energy: Int,
	@Json(name = "energy_100g") val energyPer100g: Int,
	@Json(name = "energy_value") val energyValue: Int,
	@Json(name = "energy_serving") val energyPerServing: Int,
	@Json(name = "energy_unit") val energyUnit: String,

	@Json(name = "fat") val fat: Int,
	@Json(name = "fat_100g") val fatPer100g: Int,
	@Json(name = "fat_value") val fatValue: Int,
	@Json(name = "fat_serving") val fatPerServing: Int,
	@Json(name = "fat_unit") val fatUnit: String,
	@Json(name = "saturated-fat") val saturatedFat: Int,
	@Json(name = "saturated-fat_100g") val saturatedFatPer100g: Int,
	@Json(name = "saturated-fat_value") val saturatedFatValue: Int,
	@Json(name = "saturated-fat_serving") val saturatedFatPerServing: Int,
	@Json(name = "saturated-fat_unit") val saturatedFatUnit: String,

	@Json(name = "carbohydrates") val carbohydrates: Int,
	@Json(name = "carbohydrates_100g") val carbohydratesPer100g: Int,
	@Json(name = "carbohydrates_value") val carbohydratesValue: Int,
	@Json(name = "carbohydrates_serving") val carbohydratesPerServing: Int,
	@Json(name = "carbohydrates_unit") val carbohydratesUnit: String,
	@Json(name = "sugars") val sugars: Int,
	@Json(name = "sugars_100g") val sugarsPer100g: Int,
	@Json(name = "sugars_value") val sugars_value: Int,
	@Json(name = "sugars_serving") val sugarsPerServing: Int,
	@Json(name = "sugars_unit") val sugarsUnit: String,

	@Json(name = "proteins") val proteins: Int,
	@Json(name = "proteins_100g") val proteinsPer100g: Int,
	@Json(name = "proteins_value") val proteinsValue: Int,
	@Json(name = "proteins_serving") val proteinsPerServing: Int,
	@Json(name = "proteins_unit") val proteinsUnit: String,

	@Json(name = "salt") val salt: Double,
	@Json(name = "salt_100g") val saltPer100g: Double,
	@Json(name = "salt_value") val saltValue: Double,
	@Json(name = "salt_serving") val saltPerServing: Double,
	@Json(name = "salt_unit") val saltUnit: String,
	@Json(name = "sodium") val sodium: Double,
	@Json(name = "sodium_100g") val sodiumPer100g: Double,
	@Json(name = "sodium_value") val sodiumValue: Double,
	@Json(name = "sodium_serving") val sodiumPerServing: Double,
	@Json(name = "sodium_unit") val sodiumUnit: String,
)