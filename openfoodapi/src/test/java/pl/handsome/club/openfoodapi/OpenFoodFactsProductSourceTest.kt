package pl.handsome.club.openfoodapi

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.DefaultAppDispatchers
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.openfoodapi.api.OPEN_FOOD_FACTS_BASE_URL
import pl.handsome.club.openfoodapi.api.UserAgentInterceptor
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsImageOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit


private const val TEST_USER_AGENT = "KetoScanner/test (contact)"

class OpenFoodFactsProductSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var source: OpenFoodFactsProductSource


    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        source = OpenFoodFactsProductSource(
            createOpenFoodFactsApi(
                createOpenFoodFactsRetrofit(
                    server.url("/").toString(),
                    createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, false),
                    createOpenFoodFactsMoshi()
                )
            ),
            DefaultAppDispatchers
        )
    }

    @After
    fun tearDown() {
        server.close()
    }


    @Test
    fun `found product is parsed into success`() {
        enqueue(200, fixture("product_found.json"))

        val product = successValue(runBlocking { source.getProduct("04963406") })

        assertThat(product.name).isEqualTo("Coke Original Taste")
        assertThat(product.barcode).isEqualTo("04963406")
        assertThat(product.brand).isEqualTo("Coke")
        assertThat(product.servingAmount).isEqualTo("355")
        assertThat(product.nutriments.energyPer100g).isWithin(1e-4).of(39.4366)
        assertThat(product.nutriments.carbohydratesPer100g).isWithin(1e-4).of(10.9859)
        assertThat(product.nutriments.carbohydratesPerServing).isEqualTo(39.0)
        assertThat(product.nutriments.saltPerServing).isEqualTo(0.0317)
    }

    @Test
    fun `http 404 is mapped to not found`() {
        enqueue(404, fixture("product_not_found.json"))

        val result = runBlocking { source.getProduct("5900000000017") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.NotFound))
    }

    @Test
    fun `invalid code response without product is mapped to not found`() {
        enqueue(200, fixture("invalid_code.json"))

        val result = runBlocking { source.getProduct("asd") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.NotFound))
    }

    @Test
    fun `product without name nutriments and serving is parsed into success`() {
        enqueue(200, fixture("product_minimal.json"))

        val product = successValue(runBlocking { source.getProduct("3017620422003") })

        assertThat(product.name).isEmpty()
        assertThat(product.nutriments)
            .isEqualTo(ProductNutriments(null, null, null, null, null, null, null, null, null, null))
        assertThat(product.servingAmount).isNull()
    }

    @Test
    fun `server error is mapped to http failure with its code`() {
        enqueue(500, "")

        val result = runBlocking { source.getProduct("04963406") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.Http(500)))
    }

    @Test
    fun `malformed json is mapped to parsing failure`() {
        enqueue(200, "not json")

        val result = runBlocking { source.getProduct("04963406") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.Parsing))
    }

    @Test
    fun `json with a wrong field type is mapped to parsing failure`() {
        enqueue(200, """{"status":"one","code":"04963406"}""")

        val result = runBlocking { source.getProduct("04963406") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.Parsing))
    }

    @Test
    fun `disconnect is mapped to network failure`() {
        repeat(3) {
            server.enqueue(MockResponse.Builder().onRequestStart(SocketEffect.ShutdownConnection).build())
        }

        val result = runBlocking { source.getProduct("04963406") }

        assertThat(result).isEqualTo(Outcome.Failure(AppError.Network))
    }

    @Test
    fun `request uses v2 path fields filter and user agent`() {
        enqueue(200, fixture("product_found.json"))

        runBlocking { source.getProduct("04963406") }

        val request = server.takeRequest()
        assertThat(request.url.encodedPath).isEqualTo("/api/v2/product/04963406")
        assertThat(request.url.queryParameter("fields"))
            .isEqualTo("product_name,brands,image_url,serving_quantity,nutriments")
        assertThat(request.url.queryParameter("lc")).isEqualTo("pl")
        assertThat(request.headers["User-Agent"]).isEqualTo(TEST_USER_AGENT)
    }

    @Test
    fun `base url points to the world host`() {
        assertThat(OPEN_FOOD_FACTS_BASE_URL).isEqualTo("https://world.openfoodfacts.org/")
    }

    @Test
    fun `api client has default timeouts`() {
        val client = createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, enableBodyLogging = false)

        assertThat(client.connectTimeoutMillis).isEqualTo(10_000)
        assertThat(client.readTimeoutMillis).isEqualTo(20_000)
        assertThat(client.callTimeoutMillis).isEqualTo(25_000)
    }

    @Test
    fun `image client keeps user agent and connection pool without call timeout`() {
        val apiClient = createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, enableBodyLogging = false)

        val imageClient = createOpenFoodFactsImageOkHttpClient(apiClient)

        assertThat(imageClient.callTimeoutMillis).isEqualTo(0)
        assertThat(imageClient.readTimeoutMillis).isEqualTo(30_000)
        assertThat(imageClient.connectTimeoutMillis).isEqualTo(10_000)
        assertThat(imageClient.connectionPool).isSameInstanceAs(apiClient.connectionPool)
        assertThat(imageClient.interceptors.filterIsInstance<UserAgentInterceptor>()).hasSize(1)
    }

    @Test
    fun `body logging interceptor is added only when enabled`() {
        val withLogging = createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, enableBodyLogging = true)
        val withoutLogging = createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, enableBodyLogging = false)

        val loggingInterceptors = withLogging.interceptors.filterIsInstance<HttpLoggingInterceptor>()
        assertThat(loggingInterceptors).hasSize(1)
        assertThat(loggingInterceptors.single().level).isEqualTo(HttpLoggingInterceptor.Level.BODY)
        assertThat(withoutLogging.interceptors.filterIsInstance<HttpLoggingInterceptor>()).isEmpty()
    }


    private fun successValue(outcome: Outcome<Product>): Product {
        assertThat(outcome).isInstanceOf(Outcome.Success::class.java)
        return (outcome as Outcome.Success).value
    }

    private fun enqueue(code: Int, body: String) {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource(name)!!.readText()

}
