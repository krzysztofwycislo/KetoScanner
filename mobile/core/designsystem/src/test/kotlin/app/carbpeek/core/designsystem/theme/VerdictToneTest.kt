package app.carbpeek.core.designsystem.theme

import app.carbpeek.core.designsystem.component.Rating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VerdictToneTest {

    private val verdict = LightAppColors.verdict

    @Test
    fun positiveMapsToPositiveTone() {
        assertEquals(verdict.positive, verdict.toneFor(Rating.Positive))
    }

    @Test
    fun neutralMapsToNeutralTone() {
        assertEquals(verdict.neutral, verdict.toneFor(Rating.Neutral))
    }

    @Test
    fun negativeMapsToNegativeTone() {
        assertEquals(verdict.negative, verdict.toneFor(Rating.Negative))
    }

    @Test
    fun unknownMapsToUnknownToneNotNegative() {
        assertEquals(verdict.unknown, verdict.toneFor(Rating.Unknown))
        assertNotEquals(verdict.negative, verdict.toneFor(Rating.Unknown))
    }

    @Test
    fun everyRatingHasItsOwnTone() {
        assertEquals(Rating.entries.size, Rating.entries.map { verdict.toneFor(it) }.toSet().size)
    }
}
