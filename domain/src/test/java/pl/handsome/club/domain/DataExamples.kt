package pl.handsome.club.domain

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500

private const val PRODUCT_NAME: String = "test"
private const val PRODUCT_BARCODE: String = "12345679"

fun createProduct(productNutriments: ProductNutriments) = Product(PRODUCT_NAME, PRODUCT_BARCODE, productNutriments)

fun createProduct(name: String, productNutriments: ProductNutriments) = Product(name, PRODUCT_BARCODE, productNutriments)


val exampleNutrients = ProductNutriments(30.0, 30.0, 30.0)
