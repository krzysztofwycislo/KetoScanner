package pl.handsome.club.ketoscanner.repository

import pl.handsome.club.domain.data.Product
import pl.handsome.club.openfoodapi.data.GetProductResponse


fun convertToProduct(response: GetProductResponse): Product {
    val product = response.apiProduct
    return Product(
        product.productName,
        response.barcode
    )
}