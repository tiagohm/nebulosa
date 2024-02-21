package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.GET

interface AlpacaDeviceManagementService {

    @GET("management/v1/configureddevices")
    fun configuredDevices(): Call<ListResponse<ConfiguredDevice>>
}
