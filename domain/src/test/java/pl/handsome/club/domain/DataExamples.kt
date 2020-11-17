package pl.handsome.club.domain

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500

private const val PRODUCT_NAME = "test name"
private const val PRODUCT_BARCODE = "test barcode"
private const val PRODUCT_COMPANY_NAME = "test company name"
private const val PRODUCT_URL = "test url"
private const val PRODUCT_SERVING = "50"

fun createProduct(name: String, productNutriments: ProductNutriments) =
    Product(name, PRODUCT_BARCODE, PRODUCT_COMPANY_NAME, productNutriments, PRODUCT_URL, PRODUCT_SERVING)

fun createProduct(productNutriments: ProductNutriments) =
    createProduct(PRODUCT_NAME, productNutriments)


val exampleNutrients = ProductNutriments(250.0, 500.0, 20.0, 40.0, 10.0, 20.0, 5.0, 10.0, 1.0, 2.0)
