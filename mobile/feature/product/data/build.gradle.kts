plugins {
    id("carbpeek.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.carbpeek.feature.product.data"
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))
    implementation(project(":openfoodapi"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
