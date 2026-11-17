package app.carbpeek.server.db

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class JdbcDatabaseHealthCheckPausedTest {

    @Test
    fun `when the database stops answering then it reports down within 3 seconds`() {
        startPostgresContainer().use { container ->
            container.createTestDataSource().use { dataSource ->
                val healthCheck = JdbcDatabaseHealthCheck(dataSource)
                val dockerClient = container.dockerClient
                runBlocking { assertTrue(healthCheck.isUp()) }

                dockerClient.pauseContainerCmd(container.containerId).exec()
                try {
                    repeat(3) { attempt ->
                        runBlocking {
                            delay(600.milliseconds)
                            val (isUp, duration) = measureTimedValue { healthCheck.isUp() }

                            assertFalse(actual = isUp, message = "attempt $attempt reported up")
                            assertTrue(
                                actual = duration < 3.seconds,
                                message = "attempt $attempt took $duration",
                            )
                        }
                    }
                } finally {
                    dockerClient.unpauseContainerCmd(container.containerId).exec()
                }
            }
        }
    }
}
