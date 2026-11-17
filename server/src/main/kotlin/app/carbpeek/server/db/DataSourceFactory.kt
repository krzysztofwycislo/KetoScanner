package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun createDataSource(config: DatabaseConfig): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.url
        username = config.user
        password = config.password
        maximumPoolSize = config.poolSize
        connectionTimeout = 2_000
        validationTimeout = 1_000
        initializationFailTimeout = -1
    }
    return HikariDataSource(hikariConfig)
}
