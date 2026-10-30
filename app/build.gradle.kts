plugins {
    alias(libs.plugins.android.application)
    id("ketoscanner.app-version")
    id("ketoscanner.compose")
}

android {
    namespace = "pl.handsome.club.ketoscanner"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pl.handsome.club.ketoscanner"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":feature:analysis:api"))
    implementation(project(":feature:history:api"))
    implementation(project(":feature:analysis:domain"))
    implementation(project(":openfoodapi"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:history:data"))
    implementation(project(":feature:product:data"))
    implementation(project(":feature:history:ui"))
    implementation(project(":feature:home:ui"))
    implementation(project(":feature:product:ui"))
    implementation(project(":feature:scanner:ui"))
    implementation(project(":feature:favourites:data"))
    implementation(project(":feature:favourites:ui"))
    implementation(project(":feature:preferences:data"))
    implementation(project(":feature:preferences:ui"))
    implementation(project(":feature:about:ui"))
    implementation(project(":core:datastore"))

    // kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // di
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    // maintenance
    debugImplementation(libs.leakcanary.android)
}
