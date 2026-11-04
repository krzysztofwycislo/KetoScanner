plugins {
    id("karbo.kotlin.library")
}

dependencies {
    api(project(":feature:analysis:api"))
    api(project(":feature:preferences:api"))
    api(platform(libs.koin.bom))
    api(libs.koin.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
