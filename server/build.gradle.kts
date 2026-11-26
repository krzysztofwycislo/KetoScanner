plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    sourceSets.main {
        kotlin.srcDir("src/generated/jooq")
    }
}

val jooqCodegen by sourceSets.creating
val migrationsDir = layout.projectDirectory.dir("src/main/resources/db/migration")
val generatedJooqDir = layout.projectDirectory.dir("src/generated/jooq")

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.negotiation)
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

    jooqCodegen.implementationConfigurationName(libs.jooq.codegen)
    jooqCodegen.implementationConfigurationName(libs.flyway.core)
    jooqCodegen.implementationConfigurationName(libs.testcontainers.postgresql)
    jooqCodegen.runtimeOnlyConfigurationName(libs.flyway.database.postgresql)
    jooqCodegen.runtimeOnlyConfigurationName(libs.postgresql)
    jooqCodegen.runtimeOnlyConfigurationName(libs.logback.classic)
}

tasks.register<JavaExec>("generateJooq") {
    group = "jooq"
    classpath = jooqCodegen.runtimeClasspath
    mainClass.set("app.carbpeek.server.codegen.JooqCodegenKt")
    args(
        migrationsDir.asFile.absolutePath,
        generatedJooqDir.asFile.absolutePath,
        postgresImage,
    )
    systemProperties(jooqSystemProperties)
    inputs.dir(migrationsDir)
    outputs.dir(generatedJooqDir)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnit()
            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
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
