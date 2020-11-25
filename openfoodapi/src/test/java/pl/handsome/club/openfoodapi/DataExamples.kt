package pl.handsome.club.openfoodapi

import pl.handsome.club.openfoodapi.data.ApiNutriments
import pl.handsome.club.openfoodapi.data.ApiProduct
import pl.handsome.club.openfoodapi.data.GetProductResponse


private const val PRODUCT_NAME = "test name"
private const val PRODUCT_BARCODE = "test barcode"
private const val PRODUCT_URL = "test url"
private const val PRODUCT_BRAND = "test url"
private const val PRODUCT_SERVING = "50"

fun getProductResponseWithProduct(): GetProductResponse =
    GetProductResponse(1, PRODUCT_BARCODE, testApiProduct())

fun getProductResponseWithoutProduct(): GetProductResponse =
    GetProductResponse(0, PRODUCT_BARCODE, null)

private fun testApiProduct(): ApiProduct = ApiProduct(
    testApiNutriments(),
    PRODUCT_NAME,
    PRODUCT_URL,
    PRODUCT_BRAND,
    PRODUCT_SERVING
)

private fun testApiNutriments(): ApiNutriments = ApiNutriments(
    512.0,
    206.0,
    30.0,
    15.0,
    14.0,
    7.0,
    44.0,
    22.0,
    10.0,
    5.0,
    20.0,
    10.0,
    3.0,
    1.5
)