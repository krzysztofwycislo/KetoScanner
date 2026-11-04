package pl.morning.tower.karbo.core.designsystem.text

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UiTextTest {

    @Test
    fun `when Res is built with varargs then it equals Res built with the same list`() {
        val fromVarargs = UiText.Res(STRING_ID, GRAMS, TEXT)
        val fromList = UiText.Res(STRING_ID, persistentListOf(GRAMS, TEXT))

        assertEquals(fromList, fromVarargs)
    }

    @Test
    fun `when Res is built with no args then args is empty`() {
        val text = UiText.Res(STRING_ID)

        assertTrue(text.args.isEmpty())
    }

    @Test
    fun `when Plurals have equal id count and args then they are equal`() {
        val first = UiText.Plural(PLURALS_ID, 3, UiArg.Numeric.Count(3))
        val second = UiText.Plural(PLURALS_ID, 3, UiArg.Numeric.Count(3))

        assertEquals(first, second)
    }

    @Test
    fun `when Plurals differ in count then they are not equal`() {
        val first = UiText.Plural(PLURALS_ID, 1, UiArg.Numeric.Count(3))
        val second = UiText.Plural(PLURALS_ID, 2, UiArg.Numeric.Count(3))

        assertNotEquals(first, second)
    }

    @Test
    fun `when Nested args hold equal UiTexts then the args are equal`() {
        val first = UiArg.Nested(UiText.Res(STRING_ID, TEXT))
        val second = UiArg.Nested(UiText.Res(STRING_ID, TEXT))

        assertEquals(first, second)
    }

    private companion object {
        const val STRING_ID = 1
        const val PLURALS_ID = 2
        val GRAMS = UiArg.Numeric.Grams(12.5)
        val TEXT = UiArg.Text("butter")
    }
}
