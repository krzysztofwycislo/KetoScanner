package app.carbpeek.server.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppConfigTest {

    @Test
    fun `when the environment is empty then dev defaults are used`() {
        val config = AppConfig.fromEnvironment(emptyMap())

        assertEquals(
            expected = AppConfig(
                port = 8080,
                environment = Environment.DEV,
                database = DatabaseConfig(
                    url = "jdbc:postgresql://localhost:5432/carbpeek",
                    user = "carbpeek",
                    password = "carbpeek",
                    poolSize = 10,
                ),
                meta = MetaConfig(serverVersion = "dev", minSupportedVersionCode = 0),
            ),
            actual = config,
        )
    }

    @Test
    fun `when every variable is set then all values are read`() {
        val env = mapOf(
            "PORT" to "9000",
            "ENVIRONMENT" to "prod",
            "DB_URL" to "jdbc:postgresql://db:5432/prod",
            "DB_USER" to "server",
            "DB_PASSWORD" to "s3cret",
            "DB_POOL_SIZE" to "4",
            "MIN_SUPPORTED_VERSION_CODE" to "12",
            "SERVER_VERSION" to "1.2.3",
        )

        val config = AppConfig.fromEnvironment(env)

        assertEquals(
            expected = AppConfig(
                port = 9000,
                environment = Environment.PROD,
                database = DatabaseConfig(
                    url = "jdbc:postgresql://db:5432/prod",
                    user = "server",
                    password = "s3cret",
                    poolSize = 4,
                ),
                meta = MetaConfig(serverVersion = "1.2.3", minSupportedVersionCode = 12),
            ),
            actual = config,
        )
    }

    @Test
    fun `when prod has no database variables then all three are named`() {
        val error = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnvironment(mapOf("ENVIRONMENT" to "prod"))
        }

        val message = error.message.orEmpty()
        assertTrue("DB_URL" in message)
        assertTrue("DB_USER" in message)
        assertTrue("DB_PASSWORD" in message)
    }

    @Test
    fun `when the environment name is unknown then it fails`() {
        val error = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnvironment(mapOf("ENVIRONMENT" to "qa"))
        }

        assertTrue("ENVIRONMENT" in error.message.orEmpty())
    }

    @Test
    fun `when the port is not a number then it fails`() {
        val error = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnvironment(mapOf("PORT" to "eighty"))
        }

        assertTrue("PORT" in error.message.orEmpty())
    }

    @Test
    fun `when several values are invalid then every problem is named at once`() {
        val error = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnvironment(mapOf("DB_POOL_SIZE" to "many", "MIN_SUPPORTED_VERSION_CODE" to "x"))
        }

        val message = error.message.orEmpty()
        assertTrue("DB_POOL_SIZE" in message)
        assertTrue("MIN_SUPPORTED_VERSION_CODE" in message)
    }

    @Test
    fun `when the database config is printed then the password is hidden`() {
        val config = DatabaseConfig(
            url = "jdbc:postgresql://db:5432/prod",
            user = "server",
            password = "s3cret",
            poolSize = 4,
        )

        assertFalse("s3cret" in config.toString())
    }
}
