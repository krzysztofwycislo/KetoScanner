package app.carbpeek.server.codegen

import org.flywaydb.core.Flyway
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.SchemaMappingType
import org.jooq.meta.jaxb.Target
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private const val GENERATED_PACKAGE = "app.carbpeek.server.db.generated"
private val INPUT_SCHEMAS = listOf("products", "app")

fun main(args: Array<String>) {
    val (migrationsDir, targetDir, postgresImage) = args
    PostgreSQLContainer(DockerImageName.parse(postgresImage)).use { container ->
        container.start()
        migrate(container = container, migrationsDir = migrationsDir)
        generate(container = container, targetDir = targetDir)
    }
}

private fun migrate(
    container: PostgreSQLContainer,
    migrationsDir: String,
) {
    Flyway
        .configure()
        .dataSource(container.jdbcUrl, container.username, container.password)
        .locations("filesystem:$migrationsDir")
        .load()
        .migrate()
}

private fun generate(
    container: PostgreSQLContainer,
    targetDir: String,
) {
    val jdbc = Jdbc()
        .withDriver("org.postgresql.Driver")
        .withUrl(container.jdbcUrl)
        .withUser(container.username)
        .withPassword(container.password)
    val database = Database()
        .withName("org.jooq.meta.postgres.PostgresDatabase")
        .withSchemata(INPUT_SCHEMAS.map { SchemaMappingType().withInputSchema(it) })
    val generate = Generate()
        .withRecords(true)
        .withPojos(false)
        .withDaos(false)
        .withInterfaces(false)
        .withJavaTimeTypes(true)
        .withGeneratedAnnotation(false)
    val target = Target()
        .withPackageName(GENERATED_PACKAGE)
        .withDirectory(targetDir)
        .withClean(true)
    val generator = Generator()
        .withName("org.jooq.codegen.KotlinGenerator")
        .withDatabase(database)
        .withGenerate(generate)
        .withTarget(target)
    GenerationTool.generate(Configuration().withJdbc(jdbc).withGenerator(generator))
}
