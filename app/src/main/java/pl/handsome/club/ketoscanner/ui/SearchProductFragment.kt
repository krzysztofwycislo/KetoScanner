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
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import pl.handsome.club.barcodescanner.BarcodeCameraScanner
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.SearchProductFragmentBinding
import pl.handsome.club.ketoscanner.util.getNotEmptyString
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.util.onKeyEnter
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel


class SearchProductFragment : Fragment(R.layout.search_product_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by activityViewModel()

    private var barcodeCameraScanner: BarcodeCameraScanner? = null

    private var _binding: SearchProductFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SearchProductFragmentBinding.bind(view)

        if (hasCameraPermission())
            startBarcodeScanner()
        else
            askForPermissions()

        initializeView()
    }

    private fun initializeView() {
        binding.searchInput.onKeyEnter {
            onSearchFromInput()
        }

        if (BuildConfig.DEBUG) {
            binding.searchInput.setText(R.string.exampleBarcode)
        }
    }

    private fun onSearchFromInput() {
        binding.searchInput.getNotEmptyString()?.also(::searchProduct)
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
        barcodeCameraScanner = BarcodeCameraScanner(this, binding.cameraPreviewView)
            .apply {
                start()
                getScannedBarcode().observe(viewLifecycleOwner, {
                    it?.also(::searchProduct)
                })
            }
    }

    private fun askForPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissions(permissions, CAMERA_PERMISSION_RC)
    }

    // TODO converting exceptions into user messages
    private fun onProductSearchStateChanged(productAnalysisState: ProductAnalysisState?) {
        if (productAnalysisState !is ProductAnalysisState.InProgress) {
            binding.progressBar.hide()
        }

        when (productAnalysisState) {
            null -> Unit
            is ProductAnalysisState.InProgress -> binding.progressBar.show()
            is ProductAnalysisState.ProductNotFound -> showMessageAndResumeScanning(R.string.product_not_found)
            is ProductAnalysisState.Success -> navigateToAnalyzeResult()
            is ProductAnalysisState.Error -> {
                logException(productAnalysisState.throwable)
                showMessageAndResumeScanning(R.string.something_went_wrong)
            }
        }
    }

    private fun showMessageAndResumeScanning(messageId: Int) {
        barcodeCameraScanner?.resume()
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_LONG).show()
    }

    private fun navigateToAnalyzeResult() {
        SearchProductFragmentDirections
            .toProductAnalysisResultFragment()
            .let(::safeNavigateTo)
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
        _binding = null
    }

    companion object {
        private const val CAMERA_PERMISSION_RC = 1
    }
}