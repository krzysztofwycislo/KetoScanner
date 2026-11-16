package app.carbpeek.server.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

interface DatabaseHealthCheck {
    suspend fun isUp(): Boolean
}

class JdbcDatabaseHealthCheck(
    private val dataSource: DataSource,
) : DatabaseHealthCheck {
    private val logger = LoggerFactory.getLogger(JdbcDatabaseHealthCheck::class.java)

    override suspend fun isUp(): Boolean =
        try {
            withTimeoutOrNull(2.seconds) {
                runInterruptible(Dispatchers.IO) {
                    dataSource.connection.use { it.isValid(1) }
                }
            } ?: false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Database health check failed: {}", e::class.java.name)
            false
        }
}
