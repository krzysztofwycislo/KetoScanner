plugins {
    id("karbo.android.library")
}

android {
    namespace = "pl.morning.tower.karbo.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
