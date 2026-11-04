package pl.morning.tower.karbo.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorReporterTest {

    private val logger = RecordingLogger()
    private val reporter = LoggingErrorReporter(logger)

    @Test
    fun `when error is reported then one error entry with tag and message is written`() {
        reporter.report(TAG, "Loading failed", AppError.Parsing)

        assertEquals(listOf(Entry(TAG, "Loading failed: ${AppError.Parsing}", null)), logger.errors)
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun `when unknown error is reported then its cause is passed`() {
        val cause = IllegalStateException("boom")

        reporter.report(TAG, "Loading failed", AppError.Unknown(cause))

        assertEquals(cause, logger.errors.single().cause)
    }

    @Test
    fun `when non unknown error is reported then no cause is passed`() {
        reporter.report(TAG, "Loading failed", AppError.Http(500))

        assertNull(logger.errors.single().cause)
    }

    private data class Entry(val tag: String, val message: String, val cause: Throwable?)

    private class RecordingLogger : AppLogger {

        val errors = mutableListOf<Entry>()
        val warnings = mutableListOf<Entry>()

        override fun warn(tag: String, message: String) {
            warnings += Entry(tag, message, null)
        }

        override fun error(tag: String, message: String, cause: Throwable?) {
            errors += Entry(tag, message, cause)
        }
    }

    private companion object {
        const val TAG = "Tag"
    }
}
