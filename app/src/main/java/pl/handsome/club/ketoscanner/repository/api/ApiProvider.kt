package pl.handsome.club.ketoscanner.repository.api

import retrofit2.Retrofit


object ApiProvider {

    private val openFoodFactsApiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addCallAdapterFactory(ErrorHandlingAdapter())
            .build()
    }

    val openFoodFactsApi: OpenFoodFactsApi by lazy {
        openFoodFactsApiRetrofit.create(OpenFoodFactsApi::class.java)
    }

}