plugins {
    id("ketoscanner.android.library")
}

android {
    namespace = "pl.handsome.club.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
