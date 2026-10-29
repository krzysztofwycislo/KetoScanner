plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.navigation.safeargs)
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":feature:preferences:api"))
    implementation(project(":feature:analysis:api"))
    implementation(project(":feature:history:api"))
    implementation(project(":feature:favourites:api"))
    implementation(project(":feature:analysis:domain"))
    implementation(project(":openfoodapi"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:history:data"))
    implementation(project(":feature:product:data"))
    implementation(project(":feature:history:ui"))
    implementation(project(":feature:product:ui"))
    implementation(project(":feature:scanner:ui"))

    // kotlin
    implementation(libs.kotlinx.coroutines.android)

    // androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.material)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // image loading
    implementation(libs.glide)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // di
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    // test
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.test.junit)

    // maintenance
    debugImplementation(libs.leakcanary.android)
}
