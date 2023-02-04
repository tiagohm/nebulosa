package nebulosa.query.sbd

import nebulosa.query.QueryService
import okhttp3.OkHttpClient

class SmallBodyDatabaseLookupService(url: String = URL) :
    QueryService(url, clientBuilder = ::handleMultipleChoices), SmallBodyDatabaseLookup {

    private val service = retrofit.create(SmallBodyDatabaseLookup::class.java)

    override fun search(text: String) = service.search(text)

    companion object {

        const val URL = "https://ssd-api.jpl.nasa.gov/"

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
