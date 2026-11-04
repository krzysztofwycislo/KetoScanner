package pl.morning.tower.karbo.core.common

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppErrorTest {

    @Test
    fun `when error is network or not found then it is not reportable`() {
        assertFalse(AppError.Network.isReportable)
        assertFalse(AppError.NotFound.isReportable)
    }

    @Test
    fun `when error is http parsing storage or unknown then it is reportable`() {
        assertTrue(AppError.Http(500).isReportable)
        assertTrue(AppError.Http(429).isReportable)
        assertTrue(AppError.Parsing.isReportable)
        assertTrue(AppError.Storage.isReportable)
        assertTrue(AppError.Unknown(IllegalStateException("boom")).isReportable)
    }
}
