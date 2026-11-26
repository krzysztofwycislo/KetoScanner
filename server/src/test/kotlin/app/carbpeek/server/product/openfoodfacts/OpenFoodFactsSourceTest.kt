package app.carbpeek.server.product.openfoodfacts

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.product.Ingredient
import app.carbpeek.server.product.NutriScore
import app.carbpeek.server.product.Product
import app.carbpeek.server.product.ProductCategory
import app.carbpeek.server.product.noNutrients
import io.ktor.client.engine.java.Java
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val HEADERS_AND_HALF_A_BODY =
    "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 1000\r\n\r\n{\"product\":"

class OpenFoodFactsSourceTest {

    private val requests = mutableListOf<HttpRequestData>()
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respondJson("") }

    private val mockEngine = MockEngine { request ->
        requests += request
        handler(request)
    }

    private val source = createSource(requestTimeout = 6.seconds)

    private val unavailable = Outcome.Failure(ServerError.Unavailable(retryAfter = 30.seconds))

    @Test
    fun `when a product is found then it is parsed`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("product_found.json"))

            val product = successValue(source.fetch("04963406"))

            assertEquals(expected = "04963406", actual = product.barcode)
            assertEquals(expected = "Coke Original Taste", actual = product.name)
            assertEquals(expected = "Coke", actual = product.brand)
            assertEquals(expected = "https://images.openfoodfacts.org/x.jpg", actual = product.imageUrl)
            assertEquals(expected = "355", actual = product.servingQuantity)
            assertEquals(expected = 39.4366197183099, actual = product.nutrientsPer100g.energyKcal)
            assertEquals(expected = 10.9859154929577, actual = product.nutrientsPer100g.carbohydrates)
            assertEquals(expected = 39.0, actual = product.nutrientsPerServing.carbohydrates)
            assertEquals(expected = 0.0317, actual = product.nutrientsPerServing.salt)
            assertEquals(expected = 10.6, actual = product.nutrientsPer100g.sugars)
            assertEquals(expected = 37.6, actual = product.nutrientsPerServing.sugars)
            assertEquals(expected = 0.0, actual = product.nutrientsPer100g.fibre)
            assertEquals(expected = 0.0, actual = product.nutrientsPerServing.fibre)
            assertEquals(expected = ProductCategory.DRINKS, actual = product.category)
            assertNull(product.ingredients)
            assertNull(product.nutriScore)
            assertNull(product.novaGroup)
        }

    @Test
    fun `when the response has another code then the product keeps the asked barcode`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = """{"code":"4963406","product":{},"status":1}""")

            val product = successValue(source.fetch("04963406"))

            assertEquals(expected = "04963406", actual = product.barcode)
        }

    @Test
    fun `when a product has ingredients and scores then they are parsed`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("product_with_ingredients.json"))

            val product = successValue(source.fetch("3017620422003"))

            assertEquals(
                expected = listOf(
                    ingredient(id = "en:sugar", name = "Sugar"),
                    ingredient(id = "en:palm-oil", name = "Palm oil"),
                    ingredient(id = "en:hazelnut", name = "Hazelnuts", percent = 13.0),
                    ingredient(id = "en:skimmed-milk-powder", name = "Skimmed milk powder", percent = 8.7),
                    ingredient(
                        id = "en:milk-chocolate",
                        name = "Milk chocolate",
                        sub = listOf(
                            ingredient(id = "en:sugar", name = "sugar"),
                            ingredient(id = "en:cocoa-butter", name = "cocoa butter"),
                        ),
                    ),
                    ingredient(id = "en:e322", name = "Lecithins"),
                ),
                actual = product.ingredients,
            )
            assertEquals(expected = NutriScore.E, actual = product.nutriScore)
            assertEquals(expected = 4, actual = product.novaGroup)
            assertEquals(
                expected = "Sugar, palm oil, hazelnuts (13%), skimmed milk powder (8.7%), " +
                    "Milk chocolate (sugar, cocoa butter), lecithins",
                actual = product.ingredientsText,
            )
        }

    @Test
    fun `when saturated fat and the label language are present then they are parsed`() =
        runTest {
            respondWith(
                status = HttpStatusCode.OK,
                body = """
                    {
                      "product": {
                        "lang": "de",
                        "nutriments": {"saturated-fat_100g": 10.6, "saturated-fat_serving": "1.59"}
                      }
                    }
                """.trimIndent(),
            )

            val product = successValue(source.fetch("3017620422003"))

            assertEquals(expected = "de", actual = product.labelLanguage)
            assertEquals(expected = 10.6, actual = product.nutrientsPer100g.saturatedFat)
            assertEquals(expected = 1.59, actual = product.nutrientsPerServing.saturatedFat)
        }

    @Test
    fun `when the NOVA group is quoted then it is parsed`() =
        runTest {
            respondWith(
                status = HttpStatusCode.OK,
                body = """{"code":"1","product":{"nova_group":"3","nutriscore_grade":"unknown"},"status":1}""",
            )

            val product = successValue(source.fetch("00000001"))

            assertEquals(expected = 3, actual = product.novaGroup)
            assertNull(product.nutriScore)
        }

    @Test
    fun `when a product has only a brand then the other fields are null`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("product_minimal.json"))

            val product = successValue(source.fetch("3017620422003"))

            assertEquals(expected = "Nutella, Ferrero", actual = product.brand)
            assertNull(product.name)
            assertNull(product.servingQuantity)
            assertEquals(expected = noNutrients, actual = product.nutrientsPer100g)
            assertEquals(expected = noNutrients, actual = product.nutrientsPerServing)
            assertEquals(expected = ProductCategory.OTHER, actual = product.category)
        }

    @Test
    fun `when the response is 404 with status 0 and no product then the product is not found`() =
        runTest {
            respondWith(status = HttpStatusCode.NotFound, body = fixture("product_not_found.json"))

            assertEquals(
                expected = Outcome.Failure(ServerError.NotFound),
                actual = source.fetch("5900000000017"),
            )
        }

    @Test
    fun `when the response is 200 with status 0 and no product then the product is not found`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("invalid_code.json"))

            assertEquals(expected = Outcome.Failure(ServerError.NotFound), actual = source.fetch("00000001"))
        }

    @Test
    fun `when a 404 is an HTML page then it is unavailable`() =
        runTest {
            handler = {
                respond(
                    content = "<html></html>",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(name = HttpHeaders.ContentType, value = "text/html"),
                )
            }

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when there is no product and the status is not 0 then it is unavailable`() =
        runTest {
            val bodies = listOf("{}", """{"product":null}""", """{"status":1}""", """{"status":null}""")

            listOf(HttpStatusCode.NotFound, HttpStatusCode.OK).forEach { status ->
                bodies.forEach { body ->
                    respondWith(status = status, body = body)

                    assertEquals(
                        expected = unavailable,
                        actual = source.fetch("04963406"),
                        message = "$status $body",
                    )
                }
            }
        }

    @Test
    fun `when a 404 has a product then it is unavailable`() =
        runTest {
            respondWith(status = HttpStatusCode.NotFound, body = """{"status":0,"product":{}}""")

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when a server error has status 0 in the body then it is unavailable`() =
        runTest {
            respondWith(status = HttpStatusCode.BadGateway, body = fixture("product_not_found.json"))

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when the status is any other error or a redirect then it is unavailable after one request`() =
        runTest {
            val statuses = listOf(
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.Forbidden,
                HttpStatusCode.InternalServerError,
                HttpStatusCode.BadGateway,
                HttpStatusCode.Found,
            )

            statuses.forEach { status ->
                requests.clear()
                handler = {
                    respond(
                        content = "",
                        status = status,
                        headers = headersOf(name = HttpHeaders.Location, value = "https://world.openfoodfacts.org/"),
                    )
                }

                assertEquals(expected = unavailable, actual = source.fetch("04963406"), message = "$status")
                assertEquals(expected = 1, actual = requests.size, message = "$status")
            }
        }

    @Test
    fun `when the JSON is malformed or has a wrong type then it is unavailable`() =
        runTest {
            val bodies = listOf("not json", """{"product":{"nutriments":"none"}}""", """{"product":""")

            bodies.forEach { body ->
                respondWith(status = HttpStatusCode.OK, body = body)

                assertEquals(expected = unavailable, actual = source.fetch("04963406"), message = body)
            }
        }

    @Test
    fun `when the response is not JSON then it is unavailable`() =
        runTest {
            handler = {
                respond(
                    content = "<html></html>",
                    status = HttpStatusCode.OK,
                    headers = headersOf(name = HttpHeaders.ContentType, value = "text/html"),
                )
            }

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when reading fails then it is unavailable`() =
        runTest {
            handler = { throw IOException("connection reset") }

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when the request timeout is thrown then it is unavailable`() =
        runTest {
            handler = { request -> throw HttpRequestTimeoutException(request) }

            assertEquals(expected = unavailable, actual = source.fetch("04963406"))
        }

    @Test
    fun `when no response arrives in time then it is unavailable and the caller is not cancelled`() =
        runBlocking {
            handler = { awaitCancellation() }

            assertEquals(
                expected = unavailable,
                actual = createSource(requestTimeout = 100.milliseconds).fetch("04963406"),
            )
        }

    @Test
    fun `when the caller is cancelled then the cancellation is rethrown`() =
        runBlocking {
            val isRequested = CompletableDeferred<Unit>()
            handler = {
                isRequested.complete(Unit)
                awaitCancellation()
            }
            var thrown: Throwable? = null
            val call = async {
                try {
                    source.fetch("04963406")
                } catch (e: CancellationException) {
                    thrown = e
                    throw e
                }
            }
            isRequested.await()

            call.cancelAndJoin()

            assertTrue(call.isCancelled)
            assertIs<CancellationException>(thrown)
            Unit
        }

    @Test
    fun `when a product is requested then the URL the fields and the headers are the planned ones`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("product_found.json"))

            source.fetch("04963406")

            val request = requests.single()
            assertEquals(expected = URLProtocol.HTTPS, actual = request.url.protocol)
            assertEquals(expected = "world.openfoodfacts.org", actual = request.url.host)
            assertEquals(expected = "/api/v2/product/04963406", actual = request.url.encodedPath)
            assertEquals(
                expected = "product_name,brands,image_url,serving_quantity,nutriments,categories_tags,ingredients," +
                    "ingredients_text,nutriscore_grade,nova_group,lang",
                actual = request.url.parameters["fields"],
            )
            assertEquals(expected = "pl", actual = request.url.parameters["lc"])
            assertEquals(
                expected = "CarbPeek-Server/1.4.0 (support@carbpeek.app)",
                actual = request.headers[HttpHeaders.UserAgent],
            )
        }

    @Test
    fun `when the client has default timeouts then connecting takes 3 and the request 6 seconds at most`() =
        runTest {
            respondWith(status = HttpStatusCode.OK, body = fixture("product_found.json"))
            val client = createOpenFoodFactsHttpClient(engine = mockEngine, serverVersion = "1.4.0")

            OpenFoodFactsSource(client).fetch("04963406")

            val timeouts = assertNotNull(requests.single().getCapabilityOrNull(HttpTimeoutCapability))
            assertEquals(expected = 3_000L, actual = timeouts.connectTimeoutMillis)
            assertEquals(expected = 6_000L, actual = timeouts.requestTimeoutMillis)
        }

    @Test
    fun `when the real engine gets no response in time then it is unavailable`() {
        assertEquals(expected = unavailable, actual = fetchFromServerThatSends(""))
    }

    @Test
    fun `when the real engine gets only half a body in time then it is unavailable`() {
        assertEquals(expected = unavailable, actual = fetchFromServerThatSends(HEADERS_AND_HALF_A_BODY))
    }

    private fun createSource(requestTimeout: Duration): OpenFoodFactsSource =
        OpenFoodFactsSource(
            createOpenFoodFactsHttpClient(
                engine = mockEngine,
                serverVersion = "1.4.0",
                requestTimeout = requestTimeout,
            ),
        )

    private fun fetchFromServerThatSends(response: String): Outcome<Product> =
        ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { server ->
            thread(isDaemon = true) {
                val socket = server.accept()
                socket.getOutputStream().apply {
                    write(response.toByteArray())
                    flush()
                }
            }
            Java.create().use { engine ->
                val client = createOpenFoodFactsHttpClient(
                    engine = engine,
                    serverVersion = "test",
                    requestTimeout = 500.milliseconds,
                ).config { install(localServer(server.localPort)) }
                runBlocking(Dispatchers.Default) {
                    withTimeout(5.seconds) { OpenFoodFactsSource(client).fetch("04963406") }
                }
            }
        }

    private fun localServer(port: Int) =
        createClientPlugin("LocalServer") {
            onRequest { request, _ ->
                request.url.protocol = URLProtocol.HTTP
                request.url.host = "127.0.0.1"
                request.url.port = port
            }
        }

    private fun respondWith(
        status: HttpStatusCode,
        body: String,
    ) {
        handler = { respondJson(body = body, status = status) }
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData =
        respond(
            content = body,
            status = status,
            headers = headersOf(name = HttpHeaders.ContentType, value = "application/json"),
        )

    private fun successValue(outcome: Outcome<Product>): Product = assertIs<Outcome.Success<Product>>(outcome).value

    private fun ingredient(
        id: String,
        name: String,
        percent: Double? = null,
        sub: List<Ingredient> = emptyList(),
    ): Ingredient =
        Ingredient(
            id = id,
            name = name,
            labelName = name,
            declaredPercent = percent,
            subIngredients = sub,
        )

    private fun fixture(name: String): String {
        val resource = checkNotNull(javaClass.classLoader.getResource("openfoodfacts/$name")) { "Missing $name" }
        return resource.readText()
    }
}
