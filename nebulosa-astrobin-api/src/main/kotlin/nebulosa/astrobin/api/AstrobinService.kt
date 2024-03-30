package nebulosa.astrobin.api

import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.create

class AstrobinService(
    url: String = URL,
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    private val service by lazy { retrofit.create<Astrobin>() }

    fun sensors(page: Int) = service.sensors(page)

    fun sensor(id: Long) = service.sensor(id)

    fun cameras(page: Int) = service.cameras(page)

    fun camera(id: Long) = service.camera(id)

    fun telescopes(page: Int) = service.telescopes(page)

    fun telescope(id: Long) = service.telescope(id)

    companion object {

        const val URL = "https://www.astrobin.com/"
    }
}
