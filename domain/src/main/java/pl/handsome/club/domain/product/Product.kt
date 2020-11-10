package pl.handsome.club.domain.product


data class Product(
    val name: String,
    val barcode: String,
    val brand: String,
    val nutriments : ProductNutriments,
    val imageUrl: String?
)