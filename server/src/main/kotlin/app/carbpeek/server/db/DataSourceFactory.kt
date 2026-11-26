package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

internal fun createDataSource(config: DatabaseConfig): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.url
        username = config.user
        password = config.password
        maximumPoolSize = config.poolSize
        connectionTimeout = 2_000
        validationTimeout = 1_000
        initializationFailTimeout = -1
        addDataSourceProperty("options", "-c statement_timeout=2000")
        addDataSourceProperty("socketTimeout", "5")
    }
    return HikariDataSource(hikariConfig)
}
