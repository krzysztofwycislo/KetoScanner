plugins {
    id("ketoscanner.kotlin.library")
}

dependencies {
    api(project(":feature:product:api"))
    api(project(":feature:analysis:api"))
    api(libs.kotlinx.coroutines.core)
}
