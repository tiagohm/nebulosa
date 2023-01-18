package nebulosa.alpaca.api

import retrofit2.http.GET

interface Management {

    @GET("management/v1/configureddevices")
    fun configuredDevices(): AlpacaResponse<List<ConfiguredDevice>>
}
