package edu.cs371m.triviagame.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface TriviaApi {
    // This function needs to be called from a coroutine, hence the suspend
    // in its type.  Also note the @Query annotation, which says that when
    // called, retrofit will add "&difficulty=%s".format(level) to the URL
    // Thanks, retrofit!
    // Hardcode several parameters in the GET for simplicity
    // So URL can have & and ? characters

    @GET("api.php?amount=3&category=27&type=boolean")
    suspend fun getThree(@Query("difficulty") level: String) : TriviaResponse


    // I just looked at the response and "parsed" it by eye
    data class TriviaResponse(val results: List<TriviaQuestion>)

    companion object {
        // Leave this as a simple, base URL.  That way, we can have many different
        // functions (above) that access different "paths" on this server
        // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-http-url/
        var url = HttpUrl.Builder()
            .scheme("https")
            .host("opentdb.com")
            .build()

        // Public create function that ties together building the base
        // URL and the private create function that initializes Retrofit
        fun create(): TriviaApi = create(url)
        private fun create(httpUrl: HttpUrl): TriviaApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TriviaApi::class.java)
        }
    }
}