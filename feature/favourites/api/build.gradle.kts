plugins {
    id("ketoscanner.kotlin.library")
}

dependencies {
    api(project(":feature:product:api"))
    api(libs.kotlinx.coroutines.core)
}
