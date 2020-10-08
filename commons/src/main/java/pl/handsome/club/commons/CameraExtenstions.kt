package pl.handsome.club.commons

import android.hardware.camera2.CameraManager


fun getBackCameraId(cameraManager: CameraManager): String {
    with(cameraManager) {
        return cameraIdList.first {
            val characteristics = getCameraCharacteristics(it)
            characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
        }
    }
}