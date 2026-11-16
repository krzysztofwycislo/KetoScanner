package app.carbpeek.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `when health is requested then it responds ok`() = testApplication {
        application {
            module()
        }

        val response = client.get("/health")

        assertEquals(expected = HttpStatusCode.OK, actual = response.status)
        assertEquals(expected = "ok", actual = response.bodyAsText())
    }
}
