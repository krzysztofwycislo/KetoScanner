plugins {
    id("ketoscanner.android.library")
    id("ketoscanner.compose")
}

android {
    namespace = "pl.handsome.club.feature.about.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.lifecycle.viewmodel)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.compose.viewmodel)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
