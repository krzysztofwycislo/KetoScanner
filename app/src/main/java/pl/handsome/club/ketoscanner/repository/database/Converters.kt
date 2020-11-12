package pl.handsome.club.ketoscanner.repository.database

import androidx.room.TypeConverter
import pl.handsome.club.domain.analyze.macronutrient.DietRate
import java.util.*


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
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }

}
