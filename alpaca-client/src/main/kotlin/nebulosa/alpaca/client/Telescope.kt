package nebulosa.alpaca.client

import retrofit2.http.*

interface Telescope : Device {

    @GET("camera/{deviceNumber}/connected")
    override fun isConnected(@Path("deviceNumber") deviceNumber: Int): AlpacaResponse<Boolean>

    @FormUrlEncoded
    @PUT("telescope/{deviceNumber}/connected")
    override fun connect(@Path("deviceNumber") deviceNumber: Int, @Field("Connected") connected: Boolean): AlpacaResponse<Any>
}
