package pl.handsome.club.ketoscanner.repository.database

import androidx.room.TypeConverter
import pl.handsome.club.domain.analyze.macronutrient.KetoRate
import java.util.*


internal class Converters {

    @TypeConverter
    fun ketoRateToKetoRateName(ketoRate: KetoRate): String {
        return ketoRate.name
    }

    @TypeConverter
    fun ketoRateNameToKetoRate(ketoRateName: String): KetoRate {
        return KetoRate.valueOf(ketoRateName)
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
