package pl.handsome.club.ketoscanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onCameraPermissionResult
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SearchProductFragmentBinding.bind(view)

        when {
            hasCameraPermission() -> startBarcodeScanner()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showCameraPermissionRationale()
            savedInstanceState == null -> requestCameraPermission()
            // restored state: a permission request may still be in flight, don't launch a second one
            else -> Unit
        }

        initializeView()
    }

    override fun onStart() {
        super.onStart()
        // picks up a permission granted in the app Settings; never request from here (it would loop on denial)
        if (barcodeCameraScanner == null && hasCameraPermission()) {
            hideCameraPermissionPanel()
            startBarcodeScanner()
        }
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
        if (barcodeCameraScanner != null) return
        barcodeCameraScanner =BarcodeCameraScanner(this, binding.cameraPreviewView)
            .apply {
                start()
                getScannedBarcode().observe(viewLifecycleOwner, {
                    it?.also(::searchProduct)
                })
            }
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun onCameraPermissionResult(granted: Boolean) {
        when {
            granted -> {
                hideCameraPermissionPanel()
                startBarcodeScanner()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showCameraPermissionRationale()
            else -> showCameraPermissionDenied()
        }
    }

    private fun showCameraPermissionRationale() {
        showCameraPermissionPanel(R.string.camera_permission_rationale, R.string.camera_permission_allow) {
            requestCameraPermission()
        }
    }

    private fun showCameraPermissionDenied() {
        showCameraPermissionPanel(R.string.camera_permission_denied, R.string.camera_permission_open_settings) {
            openAppSettings()
        }
    }

    private fun showCameraPermissionPanel(
        @StringRes messageId: Int,
        @StringRes buttonTextId: Int,
        onButtonClick: () -> Unit
    ) {
        binding.cameraPermissionMessage.setText(messageId)
        binding.cameraPermissionButton.setText(buttonTextId)
        binding.cameraPermissionButton.setOnClickListener { onButtonClick() }
        binding.cameraPermissionPanel.isVisible = true
    }

    private fun hideCameraPermissionPanel() {
        binding.cameraPermissionPanel.isVisible = false
    }

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
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

    override fun onDestroyView() {
        super.onDestroyView()
        barcodeCameraScanner?.close()
        barcodeCameraScanner = null
        _binding = null
    }
}