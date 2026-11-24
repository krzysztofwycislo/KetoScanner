package app.carbpeek.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class HeroCardTest {

    private val titleWidth = 179.56.dp
    private val innerWidth360 = 280.dp
    private val innerWidth411 = 331.dp

    @Test
    fun `the illustration takes the room beside the title`() {
        assertEquals(88, width(availableWidth = innerWidth360, fontScale = 1f))
        assertEquals(113, width(availableWidth = innerWidth411, fontScale = 1.15f))
        assertEquals(86, width(availableWidth = innerWidth411, fontScale = 1.3f))
    }

    @Test
    fun `the illustration is at most 120 dp wide`() {
        assertEquals(120, width(availableWidth = innerWidth411, fontScale = 1f))
        assertEquals(120, width(availableWidth = 800.dp, fontScale = 1f))
    }

    @Test
    fun `there is no illustration without room beside the title`() {
        assertEquals(null, width(availableWidth = innerWidth360, fontScale = 1.15f))
        assertEquals(null, width(availableWidth = innerWidth360, fontScale = 1.3f))
        assertEquals(null, width(availableWidth = innerWidth360, fontScale = 1.5f))
        assertEquals(null, width(availableWidth = innerWidth411, fontScale = 1.5f))
    }

    @Test
    fun `80 dp beside the title and the gap are enough`() {
        assertEquals(80.dp, heroIllustrationWidth(availableWidth = 292.dp, titleWidth = 200.dp, gap = 12.dp))
        assertEquals(null, heroIllustrationWidth(availableWidth = 291.dp, titleWidth = 200.dp, gap = 12.dp))
    }

    private fun width(availableWidth: Dp, fontScale: Float): Int? =
        heroIllustrationWidth(
            availableWidth = availableWidth,
            titleWidth = titleWidth * fontScale,
            gap = 12.dp,
        )?.value?.roundToInt()
}
