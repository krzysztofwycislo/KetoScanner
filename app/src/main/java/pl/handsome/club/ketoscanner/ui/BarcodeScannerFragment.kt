package pl.handsome.club.ketoscanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.barcode_scanner_fragment.*
import pl.handsome.club.barcodescanner.BarcodeCameraScanner
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.ketoscanner.ui.parcelable.ProductParcelable
import pl.handsome.club.ketoscanner.util.logException


class BarcodeScannerFragment : Fragment(R.layout.barcode_scanner_fragment) {

    private val searchProductViewModel: SearchProductViewModel by viewModels { ViewModelFactory }

    private lateinit var barcodeCameraScanner: BarcodeCameraScanner


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeCameraScanner = BarcodeCameraScanner(this, cameraPreviewView)

        if (hasCameraPermission()) startBarcodeScanner() else askForPermissions()
    }

    private fun hasCameraPermission(): Boolean {
        val rc = checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return rc == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun startBarcodeScanner() {
        barcodeCameraScanner.start()
        barcodeCameraScanner.getScannedBarcode().observe(viewLifecycleOwner, ::onBarcodeScanned)
    }

    private fun askForPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(requireActivity(), permissions, CAMERA_PERMISSION_RC)
    }

    private fun onProductSearchStateChanged(productSearchState: ProductSearchState?) {
        when (productSearchState) {
            is ProductSearchState.InProgress -> {/* NOTHING */
            }
            is ProductSearchState.NotFound -> showMessage(R.string.product_not_found)
            is ProductSearchState.Success -> navigateToSearchResult(productSearchState.product)
            is ProductSearchState.Error -> showErrorAndResumeScanning(productSearchState.throwable)
        }
    }

    private fun showMessage(messageId: Int) {
        messageId.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG) }?.show()
    }

    // TODO converting exceptions into user messages
    private fun showErrorAndResumeScanning(throwable: Throwable) {
        barcodeCameraScanner.resume()
        logException(throwable)
        showMessage(R.string.something_went_wrong)
    }

    private fun onBarcodeScanned(barcode: String?) {
        if (barcode == null) return

        with(searchProductViewModel) {
            searchProductByBarcode(barcode)
            getProductSearchState().observe(viewLifecycleOwner, ::onProductSearchStateChanged)
        }
    }

    private fun navigateToSearchResult(product: Product) {
        BarcodeScannerFragmentDirections
            .toSearchResultFragment(ProductParcelable(product))
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
        barcodeCameraScanner.close()
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
    }
}