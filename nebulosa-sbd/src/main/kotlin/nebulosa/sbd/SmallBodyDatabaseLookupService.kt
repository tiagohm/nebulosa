package nebulosa.sbd

import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.create

class SmallBodyDatabaseLookupService(
    url: String = "https://ssd-api.jpl.nasa.gov/",
    okHttpClient: OkHttpClient? = null,
) : RetrofitService(url, okHttpClient) {

    private val service by lazy { retrofit.create<SmallBodyDatabaseLookup>() }

    override fun handleOkHttpClientBuilder(builder: OkHttpClient.Builder) {
        builder.addInterceptor {
            val response = it.proceed(it.request())

            if (response.code == 300) {
                response.newBuilder()
                    .code(200)
                    .build()
            } else {
                response
            }
        }
    }

    fun search(text: String) = service.search(text)
}
