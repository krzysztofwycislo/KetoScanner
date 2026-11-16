plugins {
    id("karbo.android.library")
}

android {
    namespace = "app.carbpeek.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
