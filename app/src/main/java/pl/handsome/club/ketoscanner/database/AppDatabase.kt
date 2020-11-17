package pl.handsome.club.ketoscanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.handsome.club.ketoscanner.database.favourite.FavouriteProductDao
import pl.handsome.club.ketoscanner.database.favourite.FavouriteProductEntity
import pl.handsome.club.ketoscanner.database.history.ProductAnalysisHistoryDao
import pl.handsome.club.ketoscanner.database.history.ProductAnalysisHistoryEntryEntity


@Database(
    entities = [
        ProductAnalysisHistoryEntryEntity::class,
        FavouriteProductEntity::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productAnalysisHistoryDao(): ProductAnalysisHistoryDao

    abstract fun favouriteProductsDao(): FavouriteProductDao


    companion object {
        fun provide(androidContext: Context) =
            Room.databaseBuilder(androidContext, AppDatabase::class.java, "ketos-db")
                .fallbackToDestructiveMigration()
                .build()
    }

}