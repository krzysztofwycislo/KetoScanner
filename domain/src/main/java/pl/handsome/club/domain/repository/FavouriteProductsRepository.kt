package pl.handsome.club.domain.repository

import androidx.paging.DataSource
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.product.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product)

    suspend fun load() : DataSource.Factory<Int, FavouriteProduct>

    suspend fun findByBarcode(barcode: String): FavouriteProduct?

}
