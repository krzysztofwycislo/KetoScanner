package app.carbpeek.server.http

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.config.HttpConfig
import app.carbpeek.server.config.MetaConfig
import app.carbpeek.server.db.DatabaseHealthCheck
import app.carbpeek.server.module
import app.carbpeek.server.product.Ingredient
import app.carbpeek.server.product.LookedUpProduct
import app.carbpeek.server.product.NutriScore
import app.carbpeek.server.product.Nutrients
import app.carbpeek.server.product.Product
import app.carbpeek.server.product.ProductCategory
import app.carbpeek.server.product.ProductLookup
import app.carbpeek.server.product.minimalProduct
import app.carbpeek.shared.contract.ApiError
import app.carbpeek.shared.contract.ApiHeaders
import app.carbpeek.shared.contract.ApiJson
import app.carbpeek.shared.contract.ApiPaths
import app.carbpeek.shared.contract.ErrorCode
import app.carbpeek.shared.contract.ErrorEnvelope
import app.carbpeek.shared.contract.NutrientAmounts
import app.carbpeek.shared.contract.ProductCategoryCode
import app.carbpeek.shared.contract.ProductIngredient
import app.carbpeek.shared.contract.ProductNutriScore
import app.carbpeek.shared.contract.ProductNutrients
import app.carbpeek.shared.contract.ProductResponse
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.KSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.koin.dsl.module as koinModule

private const val BARCODE = "3017620422003"
private const val CLOUDFLARE_IP = "CF-Connecting-IP"

class ProductRoutesTest {

    private val lookup = FakeProductLookup()

    private val product = Product(
        barcode = BARCODE,
        name = "Nutella",
        brand = "Ferrero",
        imageUrl = "https://images.openfoodfacts.org/x.jpg",
        servingQuantity = "15",
        category = ProductCategory.SPREADS,
        labelLanguage = "fr",
        ingredientsText = "Sucre, chocolat au lait 13% (lait)",
        ingredients = listOf(
            Ingredient(
                id = "en:milk-chocolate",
                name = "milk chocolate",
                labelName = "chocolat au lait",
                declaredPercent = 13.0,
                subIngredients = listOf(
                    Ingredient(
                        id = "en:milk",
                        name = "milk",
                        labelName = "lait",
                        declaredPercent = null,
                        subIngredients = emptyList(),
                    ),
                ),
            ),
        ),
        nutriScore = NutriScore.E,
        novaGroup = 4,
        nutrientsPer100g = Nutrients(
            energyKcal = 1.0,
            fat = 2.0,
            saturatedFat = 3.0,
            carbohydrates = 4.0,
            sugars = 5.0,
            fibre = 6.0,
            proteins = 7.0,
            salt = 8.0,
        ),
        nutrientsPerServing = Nutrients(
            energyKcal = 11.0,
            fat = 12.0,
            saturatedFat = 13.0,
            carbohydrates = 14.0,
            sugars = 15.0,
            fibre = 16.0,
            proteins = 17.0,
            salt = 18.0,
        ),
    )

    private val productResponse = ProductResponse(
        barcode = BARCODE,
        name = "Nutella",
        brand = "Ferrero",
        imageUrl = "https://images.openfoodfacts.org/x.jpg",
        servingQuantity = "15",
        category = ProductCategoryCode.SPREADS,
        labelLanguage = "fr",
        ingredientsText = "Sucre, chocolat au lait 13% (lait)",
        ingredients = listOf(
            ProductIngredient(
                id = "en:milk-chocolate",
                name = "milk chocolate",
                labelName = "chocolat au lait",
                declaredPercent = 13.0,
                subIngredients = listOf(ProductIngredient(id = "en:milk", name = "milk", labelName = "lait")),
            ),
        ),
        nutriScore = ProductNutriScore.E,
        novaGroup = 4,
        nutrients = ProductNutrients(
            per100g = NutrientAmounts(
                energyKcal = 1.0,
                fat = 2.0,
                saturatedFat = 3.0,
                carbohydrates = 4.0,
                sugars = 5.0,
                fibre = 6.0,
                proteins = 7.0,
                salt = 8.0,
            ),
            perServing = NutrientAmounts(
                energyKcal = 11.0,
                fat = 12.0,
                saturatedFat = 13.0,
                carbohydrates = 14.0,
                sugars = 15.0,
                fibre = 16.0,
                proteins = 17.0,
                salt = 18.0,
            ),
        ),
    )

    @Test
    fun `when a product is fresh then it responds 200 with every field and the long cache time`() =
        testServer {
            lookup.answer = Outcome.Success(LookedUpProduct(product = product, isStale = false))

            val response = client.get(ApiPaths.product(BARCODE))

            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            assertEquals(expected = productResponse, actual = response.decode(ProductResponse.serializer()))
            assertEquals(
                expected = ContentType.Application.Json,
                actual = response.contentType()?.withoutParameters(),
            )
            assertEquals(
                expected = listOf("public, max-age=3600, s-maxage=86400"),
                actual = response.headers.getAll(HttpHeaders.CacheControl),
            )
        }

    @Test
    fun `when a product is stale then it responds 200 with the short cache time`() =
        testServer {
            lookup.answer = Outcome.Success(LookedUpProduct(product = product, isStale = true))

            val response = client.get(ApiPaths.product(BARCODE))

            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            assertEquals(expected = productResponse, actual = response.decode(ProductResponse.serializer()))
            assertEquals(
                expected = listOf("public, max-age=300, s-maxage=300"),
                actual = response.headers.getAll(HttpHeaders.CacheControl),
            )
        }

    @Test
    fun `when a category or Nutri-Score is mapped then it keeps its name`() =
        testServer {
            ProductCategory.entries.forEach { category ->
                lookup.answer = found(product.copy(category = category))
                val body = client.get(ApiPaths.product(BARCODE)).decode(ProductResponse.serializer())
                assertEquals(expected = category.name, actual = body.category.name)
            }
            NutriScore.entries.forEach { nutriScore ->
                lookup.answer = found(product.copy(nutriScore = nutriScore))
                val body = client.get(ApiPaths.product(BARCODE)).decode(ProductResponse.serializer())
                assertEquals(expected = nutriScore.name, actual = body.nutriScore?.name)
            }
        }

    @Test
    fun `when a product is not found then it responds with its own 404 envelope and cache time`() =
        testServer {
            lookup.answer = Outcome.Failure(ServerError.NotFound)

            val response = client.get(ApiPaths.product(BARCODE))

            assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
            response.assertEnvelope(code = ErrorCode.NOT_FOUND, message = "Product not found")
            assertEquals(
                expected = listOf("public, max-age=300, s-maxage=600"),
                actual = response.headers.getAll(HttpHeaders.CacheControl),
            )
        }

    @Test
    fun `when the path is unknown then it responds with the general 404 envelope`() =
        testServer {
            val response = client.get("${ApiPaths.product(BARCODE)}/photos")

            assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
            response.assertEnvelope(code = ErrorCode.NOT_FOUND, message = "Not found")
            response.assertNoStore()
        }

    @Test
    fun `when the barcode is invalid then it responds 400 and nothing is looked up`() =
        testServer {
            listOf("abc", "0", "301762042200312", "49634%2006").forEach { barcode ->
                val response = client.get(ApiPaths.product(barcode))

                assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status, message = barcode)
                response.assertEnvelope(code = ErrorCode.INVALID_REQUEST, message = "Invalid barcode")
                response.assertNoStore()
            }
            assertEquals(expected = emptyList(), actual = lookup.barcodes)
        }

    @Test
    fun `when a barcode has no valid check digit or no leading zero then its normalised form is looked up`() =
        testServer {
            lookup.answer = found(minimalProduct(barcode = "04963406"))

            assertEquals(expected = HttpStatusCode.OK, actual = client.get(ApiPaths.product("04963406")).status)
            assertEquals(expected = HttpStatusCode.OK, actual = client.get(ApiPaths.product("4963406")).status)

            assertEquals(expected = listOf("04963406", "04963406"), actual = lookup.barcodes)
        }

    @Test
    fun `when the lookup is unavailable then it responds 503 with whole seconds to wait`() =
        testServer {
            val cases = mapOf(
                30.seconds to "30",
                44_100.milliseconds to "45",
                200.milliseconds to "1",
            )

            cases.forEach { (wait, header) ->
                lookup.answer = Outcome.Failure(ServerError.Unavailable(retryAfter = wait))
                val response = client.get(ApiPaths.product(BARCODE))

                assertEquals(expected = HttpStatusCode.ServiceUnavailable, actual = response.status)
                response.assertEnvelope(code = ErrorCode.UNAVAILABLE, message = "Product lookup unavailable")
                assertEquals(
                    expected = listOf(header),
                    actual = response.headers.getAll(HttpHeaders.RetryAfter),
                )
                response.assertNoStore()
            }
        }

    @Test
    fun `when the client accepts only text then a product is still answered as JSON`() =
        testServer {
            lookup.answer = found(product)

            val response = client.get(ApiPaths.product(BARCODE)) {
                headers.append(HttpHeaders.Accept, ContentType.Text.Plain.toString())
            }

            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            assertEquals(expected = productResponse, actual = response.decode(ProductResponse.serializer()))
        }

    @Test
    fun `when products are answered then there is no Vary and no rate limit header`() =
        testServer {
            listOf(found(product), Outcome.Failure(ServerError.NotFound)).forEach { answer ->
                lookup.answer = answer

                val headers = client.get(ApiPaths.product(BARCODE)).headers

                assertNull(headers[HttpHeaders.Vary])
                assertNull(headers[HttpHeaders.RetryAfter])
                assertTrue(headers.names().none { it.startsWith(prefix = "X-RateLimit", ignoreCase = true) })
            }
        }

    @Test
    fun `when an IP address has used up its limit then it responds 429 and nothing is looked up`() =
        testServer(limit = 2) {
            lookup.answer = found(product)
            repeat(2) {
                assertEquals(expected = HttpStatusCode.OK, actual = client.get(ApiPaths.product(BARCODE)).status)
            }

            val response = client.get(ApiPaths.product(BARCODE))

            assertEquals(expected = HttpStatusCode.TooManyRequests, actual = response.status)
            response.assertEnvelope(code = ErrorCode.RATE_LIMITED, message = "Too many requests")
            val waits = assertNotNull(response.headers.getAll(HttpHeaders.RetryAfter)).map { it.toInt() }
            assertTrue(actual = waits.single() in 1..60, message = "waits: $waits")
            assertTrue(response.headers.names().none { it.startsWith(prefix = "X-RateLimit", ignoreCase = true) })
            response.assertNoStore()
            assertEquals(expected = 2, actual = lookup.barcodes.size)
        }

    @Test
    fun `when the server is behind Cloudflare then each connecting IP address has its own limit`() =
        testServer(limit = 1, behindCloudflare = true) {
            lookup.answer = found(product)

            assertEquals(expected = HttpStatusCode.OK, actual = statusFor(CLOUDFLARE_IP to "192.0.2.1"))
            assertEquals(expected = HttpStatusCode.OK, actual = statusFor(CLOUDFLARE_IP to "192.0.2.2"))
            assertEquals(expected = HttpStatusCode.TooManyRequests, actual = statusFor(CLOUDFLARE_IP to "192.0.2.1"))
        }

    @Test
    fun `when the server is behind Cloudflare and the header is blank then the remote address is limited`() =
        testServer(limit = 1, behindCloudflare = true) {
            lookup.answer = found(product)

            assertEquals(expected = HttpStatusCode.OK, actual = statusFor(CLOUDFLARE_IP to " "))
            assertEquals(
                expected = HttpStatusCode.TooManyRequests,
                actual = client.get(ApiPaths.product(BARCODE)).status,
            )
        }

    @Test
    fun `when the server is not behind Cloudflare then the header is ignored`() =
        testServer(limit = 1, behindCloudflare = false) {
            lookup.answer = found(product)

            assertEquals(expected = HttpStatusCode.OK, actual = statusFor(CLOUDFLARE_IP to "192.0.2.1"))
            assertEquals(expected = HttpStatusCode.TooManyRequests, actual = statusFor(CLOUDFLARE_IP to "192.0.2.2"))
        }

    @Test
    fun `when X-Forwarded-For is sent then it changes nothing`() {
        listOf(false, true).forEach { behindCloudflare ->
            testServer(limit = 1, behindCloudflare = behindCloudflare) {
                lookup.answer = found(product)
                val header = HttpHeaders.XForwardedFor

                assertEquals(expected = HttpStatusCode.OK, actual = statusFor(header to "192.0.2.1"))
                assertEquals(expected = HttpStatusCode.TooManyRequests, actual = statusFor(header to "192.0.2.2"))
            }
        }
    }

    @Test
    fun `when the product limit is used up then health and meta still answer`() =
        testServer(limit = 1) {
            lookup.answer = found(product)
            repeat(2) { client.get(ApiPaths.product(BARCODE)) }

            repeat(3) {
                assertEquals(expected = HttpStatusCode.OK, actual = client.get(ApiPaths.HEALTH).status)
                assertEquals(expected = HttpStatusCode.OK, actual = client.get(ApiPaths.META).status)
            }
        }

    private fun testServer(
        limit: Int = 60,
        behindCloudflare: Boolean = false,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        application {
            module(
                listOf(
                    koinModule {
                        single { MetaConfig(serverVersion = "1.4.0", minSupportedVersionCode = 42) }
                        single {
                            HttpConfig(productRequestsPerMinutePerIp = limit, behindCloudflare = behindCloudflare)
                        }
                        single<DatabaseHealthCheck> { UpDatabaseHealthCheck }
                        single<ProductLookup> { lookup }
                    },
                ),
            )
        }
        block()
    }

    private suspend fun ApplicationTestBuilder.statusFor(header: Pair<String, String>): HttpStatusCode =
        client
            .get(ApiPaths.product(BARCODE)) {
                headers.append(header.first, header.second)
            }.status

    private fun found(product: Product): Outcome<LookedUpProduct> =
        Outcome.Success(LookedUpProduct(product = product, isStale = false))

    private suspend fun HttpResponse.assertEnvelope(
        code: ErrorCode,
        message: String,
    ) {
        assertEquals(
            expected = ErrorEnvelope(
                error = ApiError(
                    code = code,
                    message = message,
                    requestId = assertNotNull(headers[ApiHeaders.REQUEST_ID]),
                ),
            ),
            actual = decode(ErrorEnvelope.serializer()),
        )
    }

    private fun HttpResponse.assertNoStore() {
        assertEquals(expected = listOf("no-store"), actual = headers.getAll(HttpHeaders.CacheControl))
    }

    private suspend fun <T> HttpResponse.decode(serializer: KSerializer<T>): T =
        ApiJson.decodeFromString(deserializer = serializer, string = bodyAsText())
}

private class FakeProductLookup : ProductLookup {
    val barcodes = mutableListOf<String>()
    var answer: Outcome<LookedUpProduct>? = null

    override suspend fun find(barcode: String): Outcome<LookedUpProduct> {
        barcodes += barcode
        return checkNotNull(answer) { "The lookup was not expected to be called" }
    }
}

private object UpDatabaseHealthCheck : DatabaseHealthCheck {
    override suspend fun isUp(): Boolean = true
}
