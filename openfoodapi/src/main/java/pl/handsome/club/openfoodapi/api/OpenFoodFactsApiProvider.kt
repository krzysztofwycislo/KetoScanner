package pl.handsome.club.openfoodapi.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// TODO error handling
object OpenFoodFactsApiProvider {

    private val openFoodFactsApiRetrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor()
            .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://pl.openfoodfacts.org")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val getApi: OpenFoodFactsApi by lazy {
        openFoodFactsApiRetrofit.create(OpenFoodFactsApi::class.java)
    }

}