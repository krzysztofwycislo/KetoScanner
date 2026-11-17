package app.carbpeek.server

import app.carbpeek.server.config.AppConfig
import app.carbpeek.server.config.DatabaseConfig
import app.carbpeek.server.config.Environment
import app.carbpeek.server.config.MetaConfig
import app.carbpeek.server.config.configModule
import app.carbpeek.server.db.DatabaseHealthCheck
import app.carbpeek.shared.contract.ApiError
import app.carbpeek.shared.contract.ApiHeaders
import app.carbpeek.shared.contract.ApiJson
import app.carbpeek.shared.contract.ErrorCode
import app.carbpeek.shared.contract.ErrorEnvelope
import app.carbpeek.shared.contract.HealthResponse
import app.carbpeek.shared.contract.HealthStatus
import app.carbpeek.shared.contract.MetaResponse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.KSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import org.koin.dsl.module as koinModule

class ApplicationTest {

    private val testConfig = AppConfig(
        port = 8080,
        environment = Environment.DEV,
        database = DatabaseConfig(
            url = "jdbc:postgresql://127.0.0.1:1/none",
            user = "none",
            password = "none",
            poolSize = 1,
        ),
        meta = MetaConfig(serverVersion = "1.4.0", minSupportedVersionCode = 42),
    )

    @Test
    fun `when the database is up then health responds 200 UP`() = testServer(databaseUp = true) {
        val response = client.get("/health")

        assertEquals(expected = HttpStatusCode.OK, actual = response.status)
        assertEquals(
            expected = HealthResponse(status = HealthStatus.UP, database = HealthStatus.UP),
            actual = response.decode(HealthResponse.serializer()),
        )
    }

    @Test
    fun `when the database is down then health responds 503 DOWN`() = testServer(databaseUp = false) {
        val response = client.get("/health")

        assertEquals(expected = HttpStatusCode.ServiceUnavailable, actual = response.status)
        assertEquals(
            expected = HealthResponse(status = HealthStatus.DOWN, database = HealthStatus.DOWN),
            actual = response.decode(HealthResponse.serializer()),
        )
    }

    @Test
    fun `when meta is requested then it returns the configured values`() = testServer {
        val response = client.get("/v1/meta")

        assertEquals(expected = HttpStatusCode.OK, actual = response.status)
        assertEquals(
            expected = MetaResponse(apiVersion = 1, serverVersion = "1.4.0", minSupportedVersionCode = 42),
            actual = response.decode(MetaResponse.serializer()),
        )
    }

    @Test
    fun `when the path is unknown then it responds with the 404 envelope`() = testServer {
        val response = client.get("/v1/nothing-here")

        val requestId = assertNotNull(response.headers[ApiHeaders.REQUEST_ID])
        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
        assertEquals(
            expected = ErrorEnvelope(
                error = ApiError(code = ErrorCode.NOT_FOUND, message = "Not found", requestId = requestId),
            ),
            actual = response.decode(ErrorEnvelope.serializer()),
        )
    }

    @Test
    fun `when the method is not allowed then it responds with the 405 envelope`() = testServer {
        val response = client.post("/v1/meta")

        val requestId = assertNotNull(response.headers[ApiHeaders.REQUEST_ID])
        assertEquals(expected = HttpStatusCode.MethodNotAllowed, actual = response.status)
        assertEquals(
            expected = ErrorEnvelope(
                error = ApiError(code = ErrorCode.INVALID_REQUEST, message = "Invalid request", requestId = requestId),
            ),
            actual = response.decode(ErrorEnvelope.serializer()),
        )
    }

    @Test
    fun `when the response type is not acceptable then it responds with the 406 envelope`() = testServer {
        val response = client.get("/v1/meta") {
            headers.append(HttpHeaders.Accept, ContentType.Text.Plain.toString())
        }

        assertEquals(expected = HttpStatusCode.NotAcceptable, actual = response.status)
        assertEquals(
            expected = ErrorCode.INVALID_REQUEST,
            actual = response.decode(ErrorEnvelope.serializer()).error.code,
        )
    }

    @Test
    fun `when a route throws then it responds with the 500 envelope without the exception message`() =
        testServer {
            val response = client.get("/test/boom")

            val body = response.bodyAsText()
            val envelope = ApiJson.decodeFromString(deserializer = ErrorEnvelope.serializer(), string = body)
            assertEquals(expected = HttpStatusCode.InternalServerError, actual = response.status)
            assertEquals(expected = ErrorCode.INTERNAL, actual = envelope.error.code)
            assertEquals(expected = response.headers[ApiHeaders.REQUEST_ID], actual = envelope.error.requestId)
            assertFalse("secret detail" in body)
        }

    @Test
    fun `when any path is requested then the response has a request id`() = testServer {
        val paths = listOf("/health", "/v1/meta", "/v1/nothing-here", "/test/boom")

        paths.forEach { path ->
            val response = client.get(path)

            assertNotNull(actual = response.headers[ApiHeaders.REQUEST_ID], message = "missing request id for $path")
        }
    }

    @Test
    fun `when the client sends a request id then the server generates its own`() = testServer {
        val response = client.get("/v1/meta") {
            headers.append(ApiHeaders.REQUEST_ID, "client-chosen")
        }

        val requestId = assertNotNull(response.headers[ApiHeaders.REQUEST_ID])
        assertFalse(requestId == "client-chosen")
    }

    private fun testServer(
        databaseUp: Boolean = true,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        application {
            module(
                listOf(
                    configModule(testConfig),
                    koinModule { single<DatabaseHealthCheck> { FakeDatabaseHealthCheck(isUp = databaseUp) } },
                ),
            )
            routing {
                get("/test/boom") {
                    throw IllegalStateException("secret detail")
                }
            }
        }
        block()
    }

    private suspend fun <T> HttpResponse.decode(serializer: KSerializer<T>): T =
        ApiJson.decodeFromString(deserializer = serializer, string = bodyAsText())
}

private class FakeDatabaseHealthCheck(
    private val isUp: Boolean,
) : DatabaseHealthCheck {
    override suspend fun isUp(): Boolean = isUp
}
