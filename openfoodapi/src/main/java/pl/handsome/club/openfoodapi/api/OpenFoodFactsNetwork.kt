package pl.handsome.club.openfoodapi.api

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"

fun createOpenFoodFactsMoshi(): Moshi = Moshi.Builder().build()

fun createOpenFoodFactsOkHttpClient(
    userAgent: String,
    enableBodyLogging: Boolean,
    connectTimeout: Duration = 10.seconds,
    readTimeout: Duration = 20.seconds,
    callTimeout: Duration = 25.seconds,
): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(connectTimeout)
        .readTimeout(readTimeout)
        .callTimeout(callTimeout)
        .addInterceptor(UserAgentInterceptor(userAgent))
        .apply {
            if (enableBodyLogging) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }
        .build()

fun createOpenFoodFactsImageOkHttpClient(apiClient: OkHttpClient, readTimeout: Duration = 30.seconds): OkHttpClient =
    apiClient.newBuilder()
        .readTimeout(readTimeout)
        .callTimeout(Duration.ZERO)
        .build()

fun createOpenFoodFactsRetrofit(baseUrl: String, client: OkHttpClient, moshi: Moshi): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

fun createOpenFoodFactsApi(retrofit: Retrofit): OpenFoodFactsApi =
    retrofit.create(OpenFoodFactsApi::class.java)


internal class UserAgentInterceptor(private val userAgent: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }

}
