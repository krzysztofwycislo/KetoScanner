package pl.handsome.club.ketoscanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.search_product_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.handsome.club.barcodescanner.BarcodeCameraScanner
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.getNotEmptyString
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.util.onKeyEnter
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel


class SearchProductFragment : Fragment(R.layout.search_product_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()

    private var barcodeCameraScanner: BarcodeCameraScanner? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasCameraPermission())
            startBarcodeScanner()
        else
            askForPermissions()

        initializeView()
    }

    private fun initializeView() {
        searchInput.onKeyEnter {
            onSearchFromInput()
        }

        if (BuildConfig.DEBUG) {
            searchInput.setText(R.string.exampleBarcode)
        }
    }

    private fun onSearchFromInput() {
        searchInput.getNotEmptyString()?.also(::searchProduct)
    }

    private fun searchProduct(barcode: String) {
        with(analyzeProductViewModel) {
            searchAndAnalyzeProduct(barcode)
            getProductAnalysisState().observe(viewLifecycleOwner, ::onProductSearchStateChanged)
        }
    }

    private fun hasCameraPermission(): Boolean {
        val rc = checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return rc == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun startBarcodeScanner() {
        barcodeCameraScanner = BarcodeCameraScanner(this, cameraPreviewView)
            .apply {
                start()
                getScannedBarcode().observe(viewLifecycleOwner, ::onBarcodeScanned)
            }
    }

    private fun askForPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissions(permissions, CAMERA_PERMISSION_RC)
    }

    private fun onProductSearchStateChanged(productAnalysisState: ProductAnalysisState?) {
        if (productAnalysisState !is ProductAnalysisState.InProgress) {
            progressBar.hide()
        }

        when (productAnalysisState) {
            is ProductAnalysisState.InProgress -> progressBar.show()
            is ProductAnalysisState.ProductNotFound -> showMessage(R.string.product_not_found)
            is ProductAnalysisState.Success -> navigateToAnalyzeResult()
            is ProductAnalysisState.Error -> showErrorAndResumeScanning(productAnalysisState.throwable)
        }
    }

    // TODO converting exceptions into user messages
    private fun showErrorAndResumeScanning(throwable: Throwable) {
        barcodeCameraScanner?.resume()
        logException(throwable)
        showMessage(R.string.something_went_wrong)
    }

    private fun showMessage(messageId: Int) {
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_LONG).show()
    }

    private fun onBarcodeScanned(barcode: String?) {
        barcode?.also(::searchProduct)
    }

    private fun navigateToAnalyzeResult() {
        SearchProductFragmentDirections
            .toProductAnalysisResultFragment()
            .let(::navigateTo)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != CAMERA_PERMISSION_RC || grantResults.isEmpty()) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            startBarcodeScanner()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        barcodeCameraScanner?.close()
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
    }
}