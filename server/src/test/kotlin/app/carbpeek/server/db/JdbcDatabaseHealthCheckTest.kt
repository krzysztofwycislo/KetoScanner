package app.carbpeek.server.db

import app.carbpeek.server.config.DatabaseConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class JdbcDatabaseHealthCheckTest {

    @Test
    fun `when the database is unreachable then it reports down within three seconds`() {
        val config = DatabaseConfig(
            url = "jdbc:postgresql://127.0.0.1:1/none",
            user = "none",
            password = "none",
            poolSize = 1,
        )

        createDataSource(config).use { dataSource ->
            val healthCheck = JdbcDatabaseHealthCheck(dataSource)

            val (isUp, duration) = measureTimedValue { runBlocking { healthCheck.isUp() } }

            assertFalse(isUp)
            assertTrue(actual = duration < 3.seconds, message = "took $duration")
        }
    }
}
