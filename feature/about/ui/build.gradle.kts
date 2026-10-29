plugins {
    id("ketoscanner.android.library")
    id("ketoscanner.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "pl.handsome.club.feature.about.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.lifecycle.viewmodel)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.compose.viewmodel)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
