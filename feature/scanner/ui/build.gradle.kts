plugins {
    id("ketoscanner.android.library")
    id("ketoscanner.compose")
}

android {
    namespace = "pl.handsome.club.feature.scanner.ui"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":barcodescanner"))

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.compose.viewmodel)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
