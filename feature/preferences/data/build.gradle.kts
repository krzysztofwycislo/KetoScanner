plugins {
    id("ketoscanner.android.library")
}

android {
    namespace = "pl.handsome.club.feature.preferences.data"
}

dependencies {
    implementation(project(":feature:preferences:api"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
