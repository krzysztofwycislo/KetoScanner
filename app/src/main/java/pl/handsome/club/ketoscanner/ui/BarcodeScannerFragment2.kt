//package pl.handsome.club.ketoscanner.ui
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.hardware.camera2.CameraAccessException
//import android.hardware.camera2.CameraCaptureSession
//import android.hardware.camera2.CameraDevice
//import android.hardware.camera2.CameraManager
//import android.os.Bundle
//import android.os.Handler
//import android.util.Log
//import android.view.Surface
//import android.view.View
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat.checkSelfPermission
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import kotlinx.android.synthetic.main.barcode_scanner_fragment.*
//import pl.handsome.club.barcodescanner.BarcodeScanner
//import pl.handsome.club.commons.getBackCameraId
//import pl.handsome.club.ketoscanner.R
//
//class BarcodeScannerFragment2 : Fragment(R.layout.barcode_scanner_fragment) {
//
//    private var barcodeScanner: BarcodeScanner? = null
//
//    private val cameraManager: CameraManager by lazy {
//        requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
//    }
//
//    private var surface: Surface? = null
//
//
//    private val cameraStateCallback = object : CameraDevice.StateCallback() {
//
//        override fun onOpened(camera: CameraDevice) {
//            try {
////                cameraPreview.holder.setFixedSize(200, 200)
//                surface = cameraPreview.holder.surface
//                val outputs = listOf<Surface>()
//
//                val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
//                    override fun onConfigured(session: CameraCaptureSession) {
//                        val previewRequest = camera
//                            .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//                            .apply { addTarget(surface!!) }
//                            .build()
//
//                        session.setRepeatingRequest(
//                            previewRequest,
//                            object : CameraCaptureSession.CaptureCallback() {},
//                            Handler { true }
//                        )
//                    }
//
//                    override fun onConfigureFailed(session: CameraCaptureSession) {
//                        TODO("Not yet implemented")
//                    }
//
//                }
//                camera.createCaptureSession(outputs, sessionStateCallback, null)
//
////                cameraPreview.setDefaultBufferSize(previewSizeFront.width, previewSizeFront.height)
//
//////                val texture = textureViewFront.surfaceTexture
//////
//////                cameraPreview.setDefaultBufferSize(previewSizeFront.width, previewSizeFront.height)
//////                val surface = Surface(texture)
////                val previewRequestBuilderFront =
////                    cameraDeviceFront!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
////                previewRequestBuilderFront.addTarget(surface)
////
////                // Here, we create a CameraCaptureSession for camera preview.
////                cameraDeviceFront?.createCaptureSession(
////                    Arrays.asList(
////                        surface,
////                        imageReaderFront?.surface
////                    ), object : CameraCaptureSession.StateCallback() {
////                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
////                            // The camera is already closed
////                            if (cameraDeviceFront == null) return
////                            // When the session is ready, we start displaying the preview.
////                            captureSessionFront = cameraCaptureSession
////                            try {
////                                // Auto focus should be continuous for camera preview.
////                                previewRequestBuilderFront.set(
////                                    CaptureRequest.CONTROL_AF_MODE,
////                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
////                                )
////                                // Finally, we start displaying the camera preview.
////                                previewRequestFront = previewRequestBuilderFront.build()
////                                captureSessionFront?.setRepeatingRequest(
////                                    previewRequestFront,
////                                    captureCallback,
////                                    backgroundHandler
////                                )
////                            } catch (e: CameraAccessException) {
////                                Log.e(TAG, e.toString())
////                            }
////                        }
////
////                        override fun onConfigureFailed(session: CameraCaptureSession) {
////                        }
////                    }, null
////                )
//            } catch (e: CameraAccessException) {
//                Log.e(TAG, e.toString())
//            }
//        }
//
//        override fun onDisconnected(camera: CameraDevice) {
//            camera.close()
//        }
//
//        override fun onError(camera: CameraDevice, error: Int) {
//            Log.e(TAG, "CameraStateCallback error occurred with id=$error")
//            camera.close()
//        }
//
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        if (hasCameraPermission())
//            onCameraPermissionGranted()
//        else
//            onCameraPermissionDenied()
//    }
//
//    private fun onCameraPermissionDenied() {
//        val permissions = arrayOf(Manifest.permission.CAMERA)
//        ActivityCompat.requestPermissions(requireActivity(), permissions, CAMERA_PERMISSION_RC)
//    }
//
//    private fun hasCameraPermission(): Boolean {
//        val rc = checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
//        return rc == PackageManager.PERMISSION_GRANTED
//    }
//
//    //checked before execution
//    @SuppressLint("MissingPermission")
//    private fun onCameraPermissionGranted() {
//        barcodeScanner = BarcodeScanner(requireContext())
//
//        val backCameraId = getBackCameraId(cameraManager)
//        cameraManager.openCamera(backCameraId, cameraStateCallback, null)
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        if (requestCode != CAMERA_PERMISSION_RC) {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//            return
//        }
//
//        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            onCameraPermissionGranted()
//        } else {
//            findNavController().navigateUp()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        barcodeScanner?.stopScanning()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        barcodeScanner?.release()
//    }
//
//    companion object {
//        private const val CAMERA_PERMISSION_RC = 1
//        private const val TAG = "BarcodeScannerFragment"
//    }
//}