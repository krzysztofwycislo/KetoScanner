package pl.handsome.club.domain

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500

private const val PRODUCT_NAME: String = "test name"
private const val PRODUCT_BARCODE: String = "test barcode"
private const val PRODUCT_COMPANY_NAME: String = "test company name"
private const val PRODUCT_URL: String = "test url"

fun createProduct(name: String, productNutriments: ProductNutriments) =
    Product(name, PRODUCT_BARCODE, PRODUCT_COMPANY_NAME, productNutriments, PRODUCT_URL)

fun createProduct(productNutriments: ProductNutriments) =
    createProduct(PRODUCT_NAME, productNutriments)


val exampleNutrients = ProductNutriments(30.0, 30.0, 30.0)
