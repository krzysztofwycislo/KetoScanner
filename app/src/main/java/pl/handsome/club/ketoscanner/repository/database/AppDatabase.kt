package pl.handsome.club.ketoscanner.repository.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [ProductAnalysisHistoryEntryEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productAnalysisHistoryDao(): ProductAnalysisHistoryDao

    companion object {
        fun provide(androidContext: Context) =
            Room.databaseBuilder(androidContext, AppDatabase::class.java, "ketos-db")
                .fallbackToDestructiveMigration()
                .build()
    }

}