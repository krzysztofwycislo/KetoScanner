package app.carbpeek.server.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

class Migrations(
    private val dataSource: DataSource,
) {
    fun migrate(): MigrateResult =
        Flyway
            .configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
}
