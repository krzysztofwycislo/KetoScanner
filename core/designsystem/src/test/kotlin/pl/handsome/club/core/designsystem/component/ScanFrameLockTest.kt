package pl.handsome.club.core.designsystem.component

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScanFrameLockTest {

    private val frame = Rect(left = 100f, top = 200f, right = 500f, bottom = 400f)
    private val designSize = Size(width = 320f, height = 240f)
    private val halfSize = Size(width = 160f, height = 120f)
    private val centreLock = Rect(left = 0.25f, top = 0.25f, right = 0.75f, bottom = 0.75f)

    @Test
    fun `when the target is inside the frame then it maps to fractions of the frame`() {
        assertEquals(
            Rect(left = 0.25f, top = 0.25f, right = 0.75f, bottom = 0.5f),
            scanFrameLockOf(Rect(left = 200f, top = 250f, right = 400f, bottom = 300f), frame),
        )
    }

    @Test
    fun `when the target is partly outside the frame then the fractions are clamped`() {
        assertEquals(
            Rect(left = 0f, top = 0.5f, right = 0.5f, bottom = 1f),
            scanFrameLockOf(Rect(left = 0f, top = 300f, right = 300f, bottom = 600f), frame),
        )
    }

    @Test
    fun `when the target is outside the frame then there is no lock`() {
        assertNull(scanFrameLockOf(Rect(left = 600f, top = 200f, right = 700f, bottom = 300f), frame))
    }

    @Test
    fun `when the frame is empty then there is no lock`() {
        assertNull(scanFrameLockOf(Rect(left = 200f, top = 250f, right = 400f, bottom = 300f), Rect.Zero))
    }

    @Test
    fun `when there is no lock at the design size then the bracket is the inset rectangle`() {
        assertEquals(
            Rect(left = 3f, top = 3f, right = 317f, bottom = 237f),
            scanFrameBracketTarget(designSize, inset = 3f, lock = null, lockPadding = 8f),
        )
    }

    @Test
    fun `when there is no lock at half size then the bracket is the inset rectangle of the smaller frame`() {
        assertEquals(
            Rect(left = 1.5f, top = 1.5f, right = 158.5f, bottom = 118.5f),
            scanFrameBracketTarget(halfSize, inset = 1.5f, lock = null, lockPadding = 8f),
        )
    }

    @Test
    fun `when there is a lock at the design size then the bracket follows the lock fractions`() {
        assertEquals(
            Rect(left = 80f, top = 60f, right = 240f, bottom = 180f),
            scanFrameBracketTarget(designSize, inset = 3f, lock = centreLock, lockPadding = 0f),
        )
    }

    @Test
    fun `when there is a lock at half size then the bracket follows the lock fractions of the smaller frame`() {
        assertEquals(
            Rect(left = 40f, top = 30f, right = 120f, bottom = 90f),
            scanFrameBracketTarget(halfSize, inset = 1.5f, lock = centreLock, lockPadding = 0f),
        )
    }

    @Test
    fun `when a padded lock reaches past the edge then the bracket is clamped to the inset rectangle`() {
        assertEquals(
            Rect(left = 3f, top = 3f, right = 170f, bottom = 130f),
            scanFrameBracketTarget(
                size = designSize,
                inset = 3f,
                lock = Rect(left = 0f, top = 0f, right = 0.5f, bottom = 0.5f),
                lockPadding = 10f,
            ),
        )
    }
}
