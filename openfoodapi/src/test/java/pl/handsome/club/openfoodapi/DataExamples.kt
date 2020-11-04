package pl.handsome.club.openfoodapi

import pl.handsome.club.openfoodapi.data.ApiNutriments
import pl.handsome.club.openfoodapi.data.ApiProduct
import pl.handsome.club.openfoodapi.data.GetProductResponse


private const val PRODUCT_NAME: String = "test name"
private const val PRODUCT_BARCODE: String = "test barcode"
private const val PRODUCT_URL: String = "test url"

fun getProductResponseWithProduct(): GetProductResponse = GetProductResponse(1, PRODUCT_BARCODE, testApiProduct())

fun getProductResponseWithoutProduct(): GetProductResponse = GetProductResponse(0, PRODUCT_BARCODE, null)

private fun testApiProduct(): ApiProduct = ApiProduct(
    2,
    testApiNutriments(),
    "emb_codes",
    listOf("additive_tag_1", "additive_tag_2"),
    listOf("ingredient_tag_1", "ingredient_tag_2"),
    PRODUCT_NAME,
    PRODUCT_URL
)

private fun testApiNutriments(): ApiNutriments = ApiNutriments(
    512.0,
    512.0,
    206.0,
    206.0,
    "kcal",
    30.0,
    30.0,
    15.0,
    15.0,
    "grams",
    14.0,
    14.0,
    7.0,
    7.0,
    "grams",
    44.0,
    44.0,
    22.0,
    22.0,
    "grams",
    10.0,
    10.0,
    5.0,
    5.0,
    "grams",
    20.0,
    20.0,
    10.0,
    10.0,
    "grams",
    3.0,
    3.0,
    1.5,
    1.5,
    "grams",
    1.0,
    1.0,
    0.5,
    0.5,
    "grams"
)