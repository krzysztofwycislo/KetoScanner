package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

fun startPostgresContainer(): PostgreSQLContainer {
    val image = checkNotNull(System.getProperty("carbpeek.postgresImage")) { "carbpeek.postgresImage is not set" }
    return PostgreSQLContainer(DockerImageName.parse(image)).apply { start() }
}

fun PostgreSQLContainer.createTestDataSource(): HikariDataSource =
    createDataSource(
        DatabaseConfig(
            url = jdbcUrl,
            user = username,
            password = password,
            poolSize = 2,
        ),
    )

object PostgresTestDatabase {
    val dataSource: HikariDataSource by lazy {
        startPostgresContainer()
            .createTestDataSource()
            .also { Migrations(it).migrate() }
    }

    val database: Database by lazy { Database(dataSource) }

    suspend fun clean() {
        database.transaction { execute("delete from app.install") }
    }
}
