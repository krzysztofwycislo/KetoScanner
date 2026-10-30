package pl.handsome.club.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.handsome.club.core.database.favourite.FavouriteProductDao
import pl.handsome.club.core.database.favourite.FavouriteProductEntity
import pl.handsome.club.core.database.history.ProductAnalysisHistoryDao
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.core.database.product.ProductDao
import pl.handsome.club.core.database.product.ProductEntity


@Database(
    entities = [
        ProductAnalysisHistoryEntryEntity::class,
        FavouriteProductEntity::class,
        ProductEntity::class
    ],
    version = 6
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productAnalysisHistoryDao(): ProductAnalysisHistoryDao

    abstract fun favouriteProductsDao(): FavouriteProductDao

    abstract fun productDao(): ProductDao


    companion object {
        fun provide(androidContext: Context) =
            Room.databaseBuilder(androidContext, AppDatabase::class.java, "ketos-db")
                .fallbackToDestructiveMigration()
                .build()
    }

}