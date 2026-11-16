plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

val postgresImage = "postgres:18"
val jooqSystemProperties = mapOf(
    "org.jooq.no-logo" to "true",
    "org.jooq.no-tips" to "true",
)

application {
    mainClass.set("app.carbpeek.server.ApplicationKt")
    applicationDefaultJvmArgs = jooqSystemProperties.map { (key, value) -> "-D$key=$value" }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.jooq)
    implementation(libs.jooq.kotlin)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.carbpeek.shared.contract)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.logback.classic)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnit()
            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(libs.kotlin.test.junit)
            }
        }

        register<JvmTestSuite>("integrationTest") {
            useJUnit()
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.postgresql)
                implementation(libs.kotlin.test.junit)
            }
            targets.configureEach {
                testTask.configure {
                    systemProperty("carbpeek.postgresImage", postgresImage)
                    systemProperties(jooqSystemProperties)
                }
            }
        }
    }
}

kotlin.target.compilations["integrationTest"].associateWith(kotlin.target.compilations["main"])
