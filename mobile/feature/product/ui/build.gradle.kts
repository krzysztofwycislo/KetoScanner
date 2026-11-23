plugins {
    id("carbpeek.android.library")
    id("carbpeek.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.carbpeek.feature.product.ui"
}

dependencies {
    implementation(project(":feature:analysis:api"))
    implementation(project(":feature:favourites:api"))
    implementation(project(":feature:product:api"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.compose.viewmodel)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
