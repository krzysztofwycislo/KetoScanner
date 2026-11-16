plugins {
    id("carbpeek.kotlin.library")
}

dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.test)
}
