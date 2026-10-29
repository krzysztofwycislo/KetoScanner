plugins {
    id("ketoscanner.kotlin.library")
}

dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
}
