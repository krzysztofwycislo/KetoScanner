package pl.morning.tower.karbo.core.designsystem.text

import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.designsystem.R
import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTextTest {

    @Test
    fun `when each error kind is mapped then it gets its own string`() {
        val expected = mapOf(
            AppError.Network to R.string.designsystem_error_network,
            AppError.Http(500) to R.string.designsystem_error_server,
            AppError.NotFound to R.string.designsystem_error_not_found,
            AppError.Parsing to R.string.designsystem_error_parsing,
            AppError.Storage to R.string.designsystem_error_storage,
            AppError.Unknown(IllegalStateException()) to R.string.designsystem_error_unknown,
        )

        expected.forEach { (error, id) -> assertEquals(UiText.Res(id), error.toUiText()) }
    }

    @Test
    fun `when http codes differ then the text is the same`() {
        assertEquals(AppError.Http(500).toUiText(), AppError.Http(503).toUiText())
    }

    @Test
    fun `when unknown error has any cause then the text is the unknown text`() {
        val expected = UiText.Res(R.string.designsystem_error_unknown)

        assertEquals(expected, AppError.Unknown(IllegalStateException("a")).toUiText())
        assertEquals(expected, AppError.Unknown(RuntimeException("b")).toUiText())
    }
}
