plugins {
    id("karbo.android.library")
}

android {
    namespace = "pl.morning.tower.karbo.feature.product.data"
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))
    implementation(project(":openfoodapi"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlin.test.junit)
}
