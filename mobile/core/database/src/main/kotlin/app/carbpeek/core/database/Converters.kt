package app.carbpeek.core.database

import androidx.room.TypeConverter
import kotlin.time.Instant


internal class Converters {

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? = instant?.toEpochMilliseconds()

}
