package pl.handsome.club.openfoodapi.api

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


const val OPEN_FOOD_FACTS_BASE_URL = "https://pl.openfoodfacts.org/"

fun createOpenFoodFactsMoshi(): Moshi = Moshi.Builder().build()

fun createOpenFoodFactsOkHttpClient(userAgent: String, enableBodyLogging: Boolean): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(userAgent))
        .apply {
            if (enableBodyLogging) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }
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
