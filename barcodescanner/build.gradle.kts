plugins {
    id("ketoscanner.android.library")
}

android {
    namespace = "pl.handsome.club.barcodescanner"
}

dependencies {
    // barcode scanning
    implementation(libs.mlkit.barcode.scanning)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    api(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    testImplementation(libs.junit)
}
