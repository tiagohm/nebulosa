package nebulosa.alpaca.api

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * The Alpaca API uses RESTful techniques and TCP/IP to enable ASCOM
 * applications and devices to communicate across modern network environments.
 *
 * @see <a href="https://ascom-standards.org/api/">ASCOM Alpaca Device API</a>
 */
class AlpacaService private constructor(retrofit: Retrofit) {

    val management by lazy { retrofit.create(Management::class.java) }

    val camera by lazy { retrofit.create(Camera::class.java) }

    val telescope by lazy { retrofit.create(Telescope::class.java) }

    constructor(url: String) : this(
        Retrofit.Builder().baseUrl(url)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(OkHttpClient.Builder().connectionPool(ConnectionPool(32, 5L, TimeUnit.MINUTES)).build())
            .build()
    )
}
