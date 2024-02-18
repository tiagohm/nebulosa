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
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    val management by lazy { retrofit.create<AlpacaDeviceManagementService>() }

    val camera by lazy { retrofit.create<AlpacaCameraService>() }

    val telescope by lazy { retrofit.create<AlpacaTelescopeService>() }

    val filterWheel by lazy { retrofit.create<AlpacaFilterWheelService>() }
}
