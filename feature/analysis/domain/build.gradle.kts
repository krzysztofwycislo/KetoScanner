plugins {
    id("carbpeek.kotlin.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":feature:analysis:api"))
    api(project(":feature:preferences:api"))
    api(platform(libs.koin.bom))
    api(libs.koin.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
}
