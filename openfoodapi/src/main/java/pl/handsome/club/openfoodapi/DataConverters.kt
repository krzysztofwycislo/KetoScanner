package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.data.Product
import pl.handsome.club.openfoodapi.data.GetProductResponse


internal fun convertToProduct(response: GetProductResponse): Product {
    val product = response.apiProduct
    return Product(
        product.productName,
        response.barcode
    )
}