plugins {
    id("karbo.android.library")
}

android {
    namespace = "pl.morning.tower.karbo.feature.history.data"
}

dependencies {
    implementation(project(":feature:history:api"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
