package nebulosa.alpaca.client

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.reflect.Type

/**
 * The Alpaca API uses RESTful techniques and TCP/IP to enable ASCOM
 * applications and devices to communicate across modern network environments.
 *
 * @see <a href="https://ascom-standards.org/api/">ASCOM Alpaca Device API</a>
 */
class AlpacaClient private constructor(retrofit: Retrofit) {

    val camera by lazy { retrofit.create(Camera::class.java) }

    val telescope by lazy { retrofit.create(Telescope::class.java) }

    constructor(url: String) : this(
        Retrofit.Builder().baseUrl(url)
            .addCallAdapterFactory(AlpacaResponseCallAdapterFactory)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    )

    private object AlpacaResponseCallAdapterFactory : CallAdapter.Factory() {

        override fun get(
            returnType: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): CallAdapter<*, *> {
            return AlpacaResposeCallAdapter<Any>()
        }
    }

    private class AlpacaResposeCallAdapter<T> : CallAdapter<AlpacaResponse<T>, AlpacaResponse<T>> {

        override fun responseType() = AlpacaResponse::class.java

        override fun adapt(call: Call<AlpacaResponse<T>>): AlpacaResponse<T> {
            val response = call.execute()

            if (response.isSuccessful) return response.body()!!
            else throw AlpacaException(response)
        }
    }
}
