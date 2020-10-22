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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.barcode_scanner_fragment.*
import pl.handsome.club.barcodescanner.BarcodeScanner
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory


class BarcodeScannerFragment : Fragment(R.layout.barcode_scanner_fragment) {

    private var barcodeScanner: BarcodeScanner? = null

    private val searchProductViewModel: SearchProductViewModel by viewModels { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasCameraPermission())
            onPermissionGranted()
        else
            onPermissionDenied()
    }

    private fun hasCameraPermission(): Boolean {
        val rc = checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return rc == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun onPermissionGranted() {
        barcodeScanner = BarcodeScanner(this, cameraPreviewView, ::onBarcodeScanned)
        barcodeScanner?.setupScanner()
    }

    private fun onPermissionDenied() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(requireActivity(), permissions, CAMERA_PERMISSION_RC)
    }

    private fun onBarcodeScanned(barcode: String) {
        barcodeScanner?.stop()
        searchProductViewModel.searchProductByBarcode(barcode)
            .observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    val direction = BarcodeScannerFragmentDirections.toSearchResultFragment(it)
                    navigateTo(direction)
                }
            })
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
            onPermissionGranted()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        barcodeScanner?.close()
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
    }
}