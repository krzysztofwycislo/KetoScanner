plugins {
    id("karbo.android.library")
    id("karbo.compose")
}

android {
    namespace = "pl.morning.tower.karbo.photocamera"
}

dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
