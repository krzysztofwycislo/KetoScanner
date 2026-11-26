package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

internal class Migrations(
    private val config: DatabaseConfig,
    private val location: String = "classpath:db/migration",
) {
    fun migrate(): MigrateResult =
        Flyway
            .configure()
            .dataSource(config.url, config.user, config.password)
            .locations(location)
            .load()
            .migrate()
}
