package app.carbpeek.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class TextSpaceTest {

    private val fontScales = listOf(1f, 1.15f, 1.3f, 1.5f)

    @Test
    fun `a 360 dp window is tight from 130 percent`() {
        assertEquals(listOf(1.3f, 1.5f), tightFontScales(windowWidth = 360.dp))
    }

    @Test
    fun `a 411 dp window is tight at 150 percent`() {
        assertEquals(listOf(1.5f), tightFontScales(windowWidth = 411.dp))
    }

    @Test
    fun `a wide window is never tight up to 150 percent`() {
        assertEquals(emptyList(), tightFontScales(windowWidth = 600.dp))
    }

    @Test
    fun `the limit itself is tight`() {
        assertEquals(true, isTextSpaceTight(windowWidth = 300.dp, fontScale = 1f))
        assertEquals(false, isTextSpaceTight(windowWidth = 301.dp, fontScale = 1f))
    }

    private fun tightFontScales(windowWidth: Dp): List<Float> =
        fontScales.filter { isTextSpaceTight(windowWidth = windowWidth, fontScale = it) }
}
