package pl.morning.tower.karbo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class BarcodeTest {

    @Test
    fun `when two barcodes have the same value then they are equal`() {
        assertEquals(Barcode(EAN), Barcode(EAN))
    }

    @Test
    fun `when barcode is created then value round-trips`() {
        assertEquals(EAN, Barcode(EAN).value)
    }

    private companion object {
        const val EAN = "5901234123457"
    }
}
