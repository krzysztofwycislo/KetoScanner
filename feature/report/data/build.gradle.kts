plugins {
    id("karbo.kotlin.library")
}

dependencies {
    implementation(project(":feature:report:api"))
    api(platform(libs.koin.bom))
    api(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.coroutines.test)
}
