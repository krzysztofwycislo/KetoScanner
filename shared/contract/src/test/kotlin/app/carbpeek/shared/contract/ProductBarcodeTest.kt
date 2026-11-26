package app.carbpeek.shared.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductBarcodeTest {

    @Test
    fun `when leading zeros are missing or extra then the code gets 8 or 13 digits`() {
        val cases = mapOf(
            "4963406" to "04963406",
            "04963406" to "04963406",
            "049000042566" to "0049000042566",
            "0000049634067" to "49634067",
            "3017620422003" to "3017620422003",
            "03017620422003" to "3017620422003",
            "2000000000008" to "2000000000008",
            "1" to "00000001",
        )

        cases.forEach { (raw, expected) ->
            assertEquals(expected = expected, actual = normalizeBarcode(raw), message = "raw: $raw")
        }
    }

    @Test
    fun `when 14 digits remain then they are kept`() {
        assertEquals(expected = "13017620422000", actual = normalizeBarcode("13017620422000"))
    }

    @Test
    fun `when a normalised code is normalised again then it stays the same`() {
        listOf("04963406", "0049000042566", "13017620422000").forEach { barcode ->
            assertEquals(expected = barcode, actual = normalizeBarcode(barcode))
        }
    }

    @Test
    fun `when the code is not 1 to 14 ASCII digits or has only zeros then it is invalid`() {
        val invalid = listOf(
            "",
            "abc",
            "49634O6",
            "0496 3406",
            " 04963406",
            "04963406\n",
            "-4963406",
            "301762042200312",
            "000000004963406",
            "0",
            "00000000",
            "٠٤٩٦٣٤٠٦",
            "４９６３４０６７",
        )

        invalid.forEach { raw ->
            assertNull(actual = normalizeBarcode(raw), message = "raw: $raw")
        }
    }
}
