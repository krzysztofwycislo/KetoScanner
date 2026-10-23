plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("appVersion") {
            id = "ketoscanner.app-version"
            implementationClass = "AppVersionPlugin"
        }
        register("kotlinLibrary") {
            id = "ketoscanner.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidLibrary") {
            id = "ketoscanner.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
