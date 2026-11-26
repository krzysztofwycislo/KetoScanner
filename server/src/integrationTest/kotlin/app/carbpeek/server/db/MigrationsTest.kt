package app.carbpeek.server.db

import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL.table
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class MigrationsTest {

    @Test
    fun `when all migrations are applied to an empty database then the schemas and the tables exist`() {
        startPostgresContainer().use { container ->
            container.createTestDataSource().use { dataSource ->
                val migrations = Migrations(container.databaseConfig())
                val database = Database(dataSource)

                val first = migrations.migrate()
                val second = migrations.migrate()

                val schemas = runBlocking {
                    database.transaction {
                        fetch("select schema_name from information_schema.schemata")
                            .getValues(0, String::class.java)
                            .toSet()
                    }
                }
                val tables = runBlocking {
                    database.transaction {
                        fetch("select table_schema || '.' || table_name from information_schema.tables")
                            .getValues(0, String::class.java)
                            .toSet()
                    }
                }
                assertEquals(expected = 2, actual = first.migrationsExecuted)
                assertEquals(expected = 0, actual = second.migrationsExecuted)
                assertTrue(schemas.containsAll(listOf("products", "app")))
                assertTrue(tables.containsAll(listOf("app.install", "products.product", "products.product_missing")))
            }
        }
    }

    @Test
    fun `when a migration runs longer than both query limits then it still finishes`() {
        startPostgresContainer().use { container ->
            container.createTestDataSource().use { dataSource ->
                val migrations = Migrations(
                    config = container.databaseConfig(),
                    location = "classpath:db/slow-migration",
                )

                val (result, duration) = measureTimedValue { migrations.migrate() }

                val markerRows = runBlocking {
                    Database(dataSource).transaction { fetchCount(table("public.slow_migration_marker")) }
                }
                assertEquals(expected = 1, actual = result.migrationsExecuted)
                assertEquals(expected = 0, actual = markerRows)
                assertTrue(actual = duration > 6.seconds, message = "took $duration")
            }
        }
    }
}
