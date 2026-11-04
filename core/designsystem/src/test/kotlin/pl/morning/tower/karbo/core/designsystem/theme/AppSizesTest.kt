package pl.morning.tower.karbo.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppSizesTest {

    private val iconScale = with(DefaultAppSizes.icon) { listOf(xs, s, m, l, xl) }
    private val controlScale = with(DefaultAppSizes.control) { listOf(xs, s, m, l, xl) }
    private val namedSizes = with(DefaultAppSizes) {
        listOf(iconTile, iconDisc, productPhoto, overlayCardMaxWidth, scanButtonLift)
    }

    @Test
    fun `every size is even`() {
        val sizes = iconScale + controlScale + namedSizes

        assertEquals(15, sizes.size)
        assertTrue(sizes.all { it.isMultipleOf(2) }, "Odd sizes in $sizes")
    }

    @Test
    fun `the smallest icon is the 16 dp base`() {
        assertEquals(16.dp, DefaultAppSizes.icon.xs)
    }

    @Test
    fun `the icon scale strictly ascends in multiples of 4`() {
        assertTrue(iconScale.strictlyAscends(), "Icon scale is out of order: $iconScale")
        assertTrue(iconScale.all { it.isMultipleOf(4) }, "Icon scale is off the grid: $iconScale")
    }

    @Test
    fun `the control scale strictly ascends in multiples of 4`() {
        assertTrue(controlScale.strictlyAscends(), "Control scale is out of order: $controlScale")
        assertTrue(controlScale.all { it.isMultipleOf(4) }, "Control scale is off the grid: $controlScale")
    }

    private fun Dp.isMultipleOf(step: Int): Boolean = value % step == 0f

    private fun List<Dp>.strictlyAscends(): Boolean = zipWithNext().all { (smaller, larger) -> smaller < larger }
}
