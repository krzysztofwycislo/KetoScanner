package app.carbpeek.shared.contract

import kotlin.test.Test
import kotlin.test.assertEquals

class HealthResponseTest {

    private val sample = HealthResponse(
        status = HealthStatus.UP,
        database = HealthStatus.UP,
    )

    @Test
    fun `when health is encoded then it matches the golden file`() {
        val encoded = ApiJson.encodeToString(serializer = HealthResponse.serializer(), value = sample)

        assertEquals(expected = parseJson(readGolden("health.json")), actual = parseJson(encoded))
    }

    @Test
    fun `when the golden file is decoded then it gives the sample`() {
        val decoded = ApiJson.decodeFromString(
            deserializer = HealthResponse.serializer(),
            string = readGolden("health.json"),
        )

        assertEquals(expected = sample, actual = decoded)
    }
}
