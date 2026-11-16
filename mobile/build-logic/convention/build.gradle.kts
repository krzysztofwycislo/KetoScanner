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
            id = "carbpeek.app-version"
            implementationClass = "AppVersionPlugin"
        }
        register("kotlinLibrary") {
            id = "carbpeek.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidLibrary") {
            id = "carbpeek.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "carbpeek.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
