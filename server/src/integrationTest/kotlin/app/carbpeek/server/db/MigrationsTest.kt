package app.carbpeek.server.db

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MigrationsTest {

    @Test
    fun `when V1 is applied to an empty database then the schemas and the install table exist`() {
        startPostgresContainer().use { container ->
            container.createTestDataSource().use { dataSource ->
                val migrations = Migrations(dataSource)
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
                val installTableExists = runBlocking {
                    database.transaction {
                        fetchExists(
                            selectOne()
                                .from("information_schema.tables")
                                .where("table_schema = 'app' and table_name = 'install'"),
                        )
                    }
                }
                assertEquals(expected = 1, actual = first.migrationsExecuted)
                assertEquals(expected = 0, actual = second.migrationsExecuted)
                assertTrue(schemas.containsAll(listOf("products", "app")))
                assertTrue(installTableExists)
            }
        }
    }
}
