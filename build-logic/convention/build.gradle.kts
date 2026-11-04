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
            id = "karbo.app-version"
            implementationClass = "AppVersionPlugin"
        }
        register("kotlinLibrary") {
            id = "karbo.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidLibrary") {
            id = "karbo.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "karbo.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
