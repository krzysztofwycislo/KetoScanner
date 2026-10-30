plugins {
    id("ketoscanner.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "pl.handsome.club.openfoodapi"
}

dependencies {
    implementation(project(":feature:product:api"))
    implementation(project(":core:common"))

    api(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
}
