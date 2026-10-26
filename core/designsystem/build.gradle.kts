plugins {
    id("ketoscanner.android.library")
    id("ketoscanner.compose")
}

android {
    namespace = "pl.handsome.club.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.kotlinx.collections.immutable)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    testImplementation(libs.kotlin.test.junit)
}
