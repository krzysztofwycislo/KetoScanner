plugins {
    id("karbo.android.library")
}

android {
    namespace = "app.carbpeek.feature.report.data"
}

dependencies {
    implementation(project(":feature:report:api"))
    implementation(project(":core:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
