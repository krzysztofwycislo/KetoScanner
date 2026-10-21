plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.api)
}

gradlePlugin {
    plugins {
        register("appVersion") {
            id = "ketoscanner.app-version"
            implementationClass = "AppVersionPlugin"
        }
    }
}
