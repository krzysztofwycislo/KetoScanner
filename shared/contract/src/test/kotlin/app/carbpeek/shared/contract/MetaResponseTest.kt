package app.carbpeek.shared.contract

import kotlin.test.Test
import kotlin.test.assertEquals

class MetaResponseTest {

    private val sample = MetaResponse(
        apiVersion = 1,
        serverVersion = "1.4.0",
        minSupportedVersionCode = 42,
    )

    @Test
    fun `when meta is encoded then it matches the golden file`() {
        val encoded = ApiJson.encodeToString(MetaResponse.serializer(), sample)

        assertEquals(expected = parseJson(readGolden("meta.json")), actual = parseJson(encoded))
    }

    @Test
    fun `when the golden file is decoded then it gives the sample`() {
        val decoded = ApiJson.decodeFromString(MetaResponse.serializer(), readGolden("meta.json"))

        assertEquals(expected = sample, actual = decoded)
    }
}
