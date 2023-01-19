package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.GET

interface Management {

    @GET("management/v1/configureddevices")
    fun configuredDevices(): Call<AlpacaResponse<List<ConfiguredDevice>>>
}
