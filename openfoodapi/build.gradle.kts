plugins {
    id("ketoscanner.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.handsome.club.openfoodapi"
}

dependencies {
    implementation(project(":domain"))

    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    api(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.okhttp.mockwebserver3)
}
