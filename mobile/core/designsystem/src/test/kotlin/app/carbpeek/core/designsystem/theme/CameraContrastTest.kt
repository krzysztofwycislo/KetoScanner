package app.carbpeek.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

private const val TEXT_CONTRAST = 4.5f
private const val SHAPE_CONTRAST = 3.0f

class CameraContrastTest {

    private val camera = LightAppColors.camera
    private val text = LightAppColors.text

    @Test
    fun `when the picture is white then the text on the buttons is readable`() {
        assertContrast(foreground = text.inverse, background = camera.control, minimum = TEXT_CONTRAST)
    }

    @Test
    fun `when the picture is white then the text on the trip chips is readable`() {
        val foregrounds = listOf(
            text.inverse,
            camera.ratingPositive,
            camera.ratingNeutral,
            camera.ratingNegative,
            camera.ratingUnknown,
        )

        foregrounds.forEach { foreground ->
            assertContrast(foreground = foreground, background = camera.chip, minimum = TEXT_CONTRAST)
        }
    }

    @Test
    fun `when the picture is white then the trip heading is readable`() {
        assertContrast(foreground = text.onHero, background = camera.hint, minimum = TEXT_CONTRAST)
    }

    @Test
    fun `when the picture is white then the white scan frame stands out from its edge`() {
        assertContrast(foreground = text.inverse, background = camera.edge, minimum = SHAPE_CONTRAST)
    }

    private fun assertContrast(foreground: Color, background: Color, minimum: Float) {
        val backgroundLuminance = background.compositeOver(Color.White).luminance()
        val contrast = (foreground.luminance() + 0.05f) / (backgroundLuminance + 0.05f)

        assertTrue(
            actual = contrast >= minimum,
            message = "$foreground on $background has a contrast of $contrast, expected at least $minimum",
        )
    }
}
