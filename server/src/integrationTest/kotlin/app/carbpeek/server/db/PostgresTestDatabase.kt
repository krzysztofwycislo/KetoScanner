package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

internal fun startPostgresContainer(): PostgreSQLContainer {
    val image = checkNotNull(System.getProperty("carbpeek.postgresImage")) { "carbpeek.postgresImage is not set" }
    return PostgreSQLContainer(DockerImageName.parse(image)).apply { start() }
}

internal fun PostgreSQLContainer.databaseConfig(): DatabaseConfig =
    DatabaseConfig(
        url = jdbcUrl,
        user = username,
        password = password,
        poolSize = 2,
    )

internal fun PostgreSQLContainer.createTestDataSource(): HikariDataSource = createDataSource(databaseConfig())

internal object PostgresTestDatabase {
    val dataSource: HikariDataSource by lazy {
        val config = startPostgresContainer().databaseConfig()
        Migrations(config).migrate()
        createDataSource(config)
    }

    val database: Database by lazy { Database(dataSource) }

    suspend fun clean() {
        database.transaction {
            execute("delete from app.install")
            execute("delete from products.product")
            execute("delete from products.product_missing")
        }
    }
}
