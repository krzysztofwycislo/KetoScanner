package pl.handsome.club.ketoscanner.database

import pl.handsome.club.feature.analysis.api.model.DietRate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant


class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `when instant is converted to epoch millis and back then it is unchanged`() {
        val instant = Instant.fromEpochMilliseconds(1_700_000_000_123)

        val epochMillis = converters.toEpochMillis(instant)

        assertEquals(1_700_000_000_123, epochMillis)
        assertEquals(instant, converters.toInstant(epochMillis))
    }

    @Test
    fun `when value is null then converters return null`() {
        assertNull(converters.toEpochMillis(null))
        assertNull(converters.toInstant(null))
    }

    @Test
    fun `when diet rate is converted to name and back then it is unchanged`() {
        DietRate.entries.forEach { dietRate ->
            val name = converters.ketoRateToKetoRateName(dietRate)

            assertEquals(dietRate, converters.ketoRateNameToKetoRate(name))
        }
    }

}
