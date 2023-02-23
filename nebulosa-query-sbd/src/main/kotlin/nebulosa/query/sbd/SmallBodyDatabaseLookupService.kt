package nebulosa.query.sbd

import nebulosa.query.QueryService
import okhttp3.OkHttpClient

class SmallBodyDatabaseLookupService(url: String = "https://ssd-api.jpl.nasa.gov/") :
    QueryService(url, clientBuilder = ::handleMultipleChoices), SmallBodyDatabaseLookup {

    private val service = retrofit.create(SmallBodyDatabaseLookup::class.java)

    override fun search(text: String) = service.search(text)

    companion object {

        @JvmStatic
        private fun handleMultipleChoices(builder: OkHttpClient.Builder) {
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
    }
}
