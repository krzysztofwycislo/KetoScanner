package pl.handsome.club.feature.product.api.model


data class Product(
    val name: String,
    val barcode: String,
    val brand: String,
    val nutriments: ProductNutriments,
    val imageUrl: String?,
    val servingAmount: String?
)