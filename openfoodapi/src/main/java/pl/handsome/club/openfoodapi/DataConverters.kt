package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.SearchProduct
import pl.handsome.club.openfoodapi.data.GetProductResponse


internal fun parseGetProductResponse(response: GetProductResponse): SearchProduct {
    if (response.apiProduct == null) {
        return SearchProduct.NotFound
    }

    val apiProduct = response.apiProduct
    val product = Product(
        apiProduct.productName,
        response.barcode
    )

    return SearchProduct.Success(product)
}