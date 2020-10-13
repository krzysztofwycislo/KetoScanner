package pl.handsome.club.barcodescanner

import android.util.SparseArray
import androidx.camera.core.ImageProxy
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy


class BarcodeImageAnalyzerTest {

    private lateinit var barcodeImageAnalyzer: BarcodeImageAnalyzer

    @Mock
    private lateinit var onBarcodeFound: (String) -> Unit

    @Mock
    private lateinit var barcodeDetector: BarcodeDetector

    @Mock
    private lateinit var mockedImageProxy: ImageProxy

    private val exampleBarcode = Barcode().apply { rawValue = "barcode" }


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        barcodeImageAnalyzer = BarcodeImageAnalyzer(onBarcodeFound, barcodeDetector)
    }


    @Test
    fun `when barcode is found onBarcodeFound should be executed`() {
        `when`(barcodeDetector.detect(any()))
            .thenReturn(SparseArray<Barcode>().apply { append(0, exampleBarcode) })

        barcodeImageAnalyzer.analyze(mockedImageProxy)

        verify(onBarcodeFound).invoke(exampleBarcode.rawValue)
    }

}