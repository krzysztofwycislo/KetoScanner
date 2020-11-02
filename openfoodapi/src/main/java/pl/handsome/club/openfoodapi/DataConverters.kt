package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.openfoodapi.data.GetProductResponse


internal fun parseGetProductResponse(response: GetProductResponse): ProductSearchState {
    if (response.apiProduct == null) {
        return ProductSearchState.NotFound
    }

    val apiProduct = response.apiProduct
    val product = Product(
        apiProduct.productName,
        response.barcode
    )

    return ProductSearchState.Success(product)
}