package pl.handsome.club.openfoodapi

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import pl.handsome.club.domain.product.ProductNutriments
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit
import retrofit2.HttpException


private const val TEST_USER_AGENT = "KetoScanner/test (contact)"

class OpenFoodFactsRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: OpenFoodFactsRepository


    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        repository = OpenFoodFactsRepository(
            createOpenFoodFactsApi(
                createOpenFoodFactsRetrofit(
                    server.url("/").toString(),
                    createOpenFoodFactsOkHttpClient(TEST_USER_AGENT, false),
                    createOpenFoodFactsMoshi()
                )
            )
        )
    }

    @After
    fun tearDown() {
        server.close()
    }


    @Test
    fun `found product is parsed into success`() {
        enqueue(200, fixture("product_found.json"))

        val result = runBlocking { repository.searchProductByBarcode("04963406") }

        assertThat(result).isInstanceOf(ProductSearchState.Success::class.java)
        val product = (result as ProductSearchState.Success).product
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

        val result = runBlocking { repository.searchProductByBarcode("5900000000017") }

        assertThat(result).isEqualTo(ProductSearchState.NotFound)
    }

    @Test
    fun `invalid code response without product is mapped to not found`() {
        enqueue(200, fixture("invalid_code.json"))

        val result = runBlocking { repository.searchProductByBarcode("asd") }

        assertThat(result).isEqualTo(ProductSearchState.NotFound)
    }

    @Test
    fun `product without name nutriments and serving is parsed into success`() {
        enqueue(200, fixture("product_minimal.json"))

        val result = runBlocking { repository.searchProductByBarcode("3017620422003") }

        assertThat(result).isInstanceOf(ProductSearchState.Success::class.java)
        val product = (result as ProductSearchState.Success).product
        assertThat(product.name).isEmpty()
        assertThat(product.nutriments)
            .isEqualTo(ProductNutriments(null, null, null, null, null, null, null, null, null, null))
        assertThat(product.servingAmount).isNull()
    }

    @Test
    fun `server error throws http exception`() {
        enqueue(500, "")

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { repository.searchProductByBarcode("04963406") }
        }

        assertThat(exception.code()).isEqualTo(500)
    }

    @Test
    fun `request uses v2 path fields filter and user agent`() {
        enqueue(200, fixture("product_found.json"))

        runBlocking { repository.searchProductByBarcode("04963406") }

        val request = server.takeRequest()
        assertThat(request.url.encodedPath).isEqualTo("/api/v2/product/04963406")
        assertThat(request.url.queryParameter("fields"))
            .isEqualTo("product_name,brands,image_url,serving_quantity,nutriments")
        assertThat(request.headers["User-Agent"]).isEqualTo(TEST_USER_AGENT)
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


    private fun enqueue(code: Int, body: String) {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource(name)!!.readText()

}
