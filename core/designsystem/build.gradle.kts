plugins {
    id("carbpeek.android.library")
    id("carbpeek.compose")
}

android {
    namespace = "app.carbpeek.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.kotlinx.collections.immutable)
    api(project(":core:common"))
    api(project(":core:model"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    testImplementation(libs.kotlin.test.junit)
}
