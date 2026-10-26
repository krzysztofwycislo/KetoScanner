plugins {
    id("ketoscanner.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.handsome.club.openfoodapi"
}

dependencies {
    implementation(project(":feature:product:api"))

    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    api(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.okhttp.mockwebserver3)
}
