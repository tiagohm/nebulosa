package nebulosa.alpaca.api

import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.create

/**
 * The Alpaca API uses RESTful techniques and TCP/IP to enable ASCOM
 * applications and devices to communicate across modern network environments.
 *
 * @see <a href="https://ascom-standards.org/api/">ASCOM Alpaca Device API</a>
 */
class AlpacaService(
    url: String,
    okHttpClient: OkHttpClient? = null,
) : RetrofitService(url, okHttpClient) {

    val management by lazy { retrofit.create<Management>() }

    val camera by lazy { retrofit.create<Camera>() }

    val telescope by lazy { retrofit.create<Telescope>() }
}
