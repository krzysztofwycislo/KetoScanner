package pl.handsome.club.core.designsystem.text

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormattingTest {

    @Test
    fun `when Grams has more digits then it is rounded to one fraction digit`() {
        assertEquals("57.5", UiArg.Numeric.Grams(57.46).format(US))
    }

    @Test
    fun `when Grams is whole then one fraction digit is kept`() {
        assertEquals("57.0", UiArg.Numeric.Grams(57.0).format(US))
    }

    @Test
    fun `when Grams is exactly half way then it rounds half up`() {
        assertEquals("57.5", UiArg.Numeric.Grams(57.45).format(US))
    }

    @Test
    fun `when Grams is formatted in German then the decimal separator is a comma`() {
        assertEquals("57,5", UiArg.Numeric.Grams(57.5).format(GERMANY))
    }

    @Test
    fun `when Grams is in the thousands then it is grouped`() {
        assertEquals("1,234.5", UiArg.Numeric.Grams(1234.5).format(US))
    }

    @Test
    fun `when Kcal is half way then it rounds half up to a whole number`() {
        assertEquals("100", UiArg.Numeric.Kcal(99.5).format(US))
    }

    @Test
    fun `when Count is in the thousands then it is grouped`() {
        assertEquals("1,234", UiArg.Numeric.Count(1234).format(US))
    }

    @Test
    fun `when WholeNumber has a fraction then it is rounded half up to a whole number`() {
        assertEquals("267", UiArg.Numeric.WholeNumber(266.6).format(US))
    }

    @Test
    fun `when Percent is a fraction then it is shown as a whole percent`() {
        assertEquals("31%", UiArg.Numeric.Percent(0.31).format(US))
    }

    private companion object {
        val US: Locale = Locale.US
        val GERMANY: Locale = Locale.GERMANY
    }
}
