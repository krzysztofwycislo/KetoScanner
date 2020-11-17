package pl.handsome.club.domain.repository

import pl.handsome.club.domain.product.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product)

}
