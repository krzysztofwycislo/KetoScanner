package pl.handsome.club.openfoodapi

import pl.handsome.club.openfoodapi.data.ApiNutriments
import pl.handsome.club.openfoodapi.data.ApiProduct
import pl.handsome.club.openfoodapi.data.GetProductResponse


fun testGetProductResponse(): GetProductResponse = GetProductResponse(1, "123456789", testApiProduct())

private fun testApiProduct(): ApiProduct = ApiProduct(
    2,
    testApiNutriments(),
    "emb_codes",
    listOf("additive_tag_1", "additive_tag_2"),
    listOf("ingredient_tag_1", "ingredient_tag_2"),
    "product_name"
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