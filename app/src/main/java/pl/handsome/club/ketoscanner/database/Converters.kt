package pl.handsome.club.ketoscanner.database

import androidx.room.TypeConverter
import pl.handsome.club.feature.analysis.api.model.DietRate
import kotlin.time.Instant


internal class Converters {

    @TypeConverter
    fun ketoRateToKetoRateName(dietRate: DietRate): String {
        return dietRate.name
    }

    @TypeConverter
    fun ketoRateNameToKetoRate(ketoRateName: String): DietRate {
        return DietRate.valueOf(ketoRateName)
    }

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? = instant?.toEpochMilliseconds()

}
