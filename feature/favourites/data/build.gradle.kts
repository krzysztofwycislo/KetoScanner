plugins {
    id("carbpeek.android.library")
}

android {
    namespace = "app.carbpeek.feature.favourites.data"
}

dependencies {
    implementation(project(":feature:favourites:api"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
