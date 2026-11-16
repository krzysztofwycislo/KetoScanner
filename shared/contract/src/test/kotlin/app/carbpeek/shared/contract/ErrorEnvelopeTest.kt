package app.carbpeek.shared.contract

import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ErrorEnvelopeTest {

    private val sample = ErrorEnvelope(
        error = ApiError(
            code = ErrorCode.NOT_FOUND,
            message = "Not found",
            requestId = "7f1c2a4e-3b6d-4c8e-9a10-2b3c4d5e6f70",
        ),
    )

    @Test
    fun `when the envelope is encoded then it matches the golden file`() {
        val encoded = ApiJson.encodeToString(ErrorEnvelope.serializer(), sample)

        assertEquals(expected = parseJson(readGolden("error-envelope.json")), actual = parseJson(encoded))
    }

    @Test
    fun `when the golden file is decoded then it gives the sample`() {
        val decoded = ApiJson.decodeFromString(ErrorEnvelope.serializer(), readGolden("error-envelope.json"))

        assertEquals(expected = sample, actual = decoded)
    }

    @Test
    fun `when the code is unknown then it decodes to UNKNOWN`() {
        val json = """{"error":{"code":"TEAPOT","message":"Short and stout"}}"""

        val decoded = ApiJson.decodeFromString(ErrorEnvelope.serializer(), json)

        assertEquals(expected = ErrorCode.UNKNOWN, actual = decoded.error.code)
    }

    @Test
    fun `when an unknown field is present then it is ignored`() {
        val json = """{"error":{"code":"INTERNAL","message":"Internal error","retryIn":5},"extra":true}"""

        val decoded = ApiJson.decodeFromString(ErrorEnvelope.serializer(), json)

        assertEquals(
            expected = ErrorEnvelope(error = ApiError(code = ErrorCode.INTERNAL, message = "Internal error")),
            actual = decoded,
        )
    }

    @Test
    fun `when the request id is null then it is omitted`() {
        val envelope = ErrorEnvelope(error = ApiError(code = ErrorCode.INTERNAL, message = "Internal error"))

        val encoded = parseJson(ApiJson.encodeToString(ErrorEnvelope.serializer(), envelope))

        assertFalse(encoded.jsonObject.getValue("error").jsonObject.containsKey("requestId"))
    }
}
