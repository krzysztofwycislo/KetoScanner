package pl.morning.tower.karbo.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.morning.tower.karbo.core.database.favourite.FavouriteProductDao
import pl.morning.tower.karbo.core.database.favourite.FavouriteProductEntity
import pl.morning.tower.karbo.core.database.history.ProductAnalysisHistoryDao
import pl.morning.tower.karbo.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.morning.tower.karbo.core.database.product.ProductDao
import pl.morning.tower.karbo.core.database.product.ProductEntity


@Database(
    entities = [
        ProductAnalysisHistoryEntryEntity::class,
        FavouriteProductEntity::class,
        ProductEntity::class
    ],
    version = 8
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