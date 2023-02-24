package nebulosa.alpaca.api

import nebulosa.retrofit.RetrofitService

/**
 * The Alpaca API uses RESTful techniques and TCP/IP to enable ASCOM
 * applications and devices to communicate across modern network environments.
 *
 * @see <a href="https://ascom-standards.org/api/">ASCOM Alpaca Device API</a>
 */
class AlpacaService(url: String) : RetrofitService(url) {

    val management by lazy { retrofit.create(Management::class.java) }

    val camera by lazy { retrofit.create(Camera::class.java) }

    val telescope by lazy { retrofit.create(Telescope::class.java) }
}
