package pl.handsome.club.ketoscanner.database.favourite

import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.database.AppDatabase


class DBFavouriteProductsRepository(
    appDatabase: AppDatabase
) : FavouriteProductsRepository {

//    private val favourit: ProductAnalysisHistoryDao =
//        appDatabase.productAnalysisHistoryDao()

    override suspend fun addToFavourites(product: Product) {
        TODO("Not yet implemented")
    }

}