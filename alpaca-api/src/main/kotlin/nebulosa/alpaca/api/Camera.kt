package nebulosa.alpaca.api

import retrofit2.http.*

interface Camera : Device {

    @GET("camera/{deviceNumber}/connected")
    override fun isConnected(@Path("deviceNumber") deviceNumber: Int): AlpacaResponse<Boolean>

    @FormUrlEncoded
    @PUT("camera/{deviceNumber}/connected")
    override fun connect(@Path("deviceNumber") deviceNumber: Int, @Field("Connected") connected: Boolean): AlpacaResponse<Any>
}
