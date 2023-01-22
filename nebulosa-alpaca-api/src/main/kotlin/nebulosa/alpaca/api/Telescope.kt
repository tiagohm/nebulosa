package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface Telescope : Device {

    @GET("telescope/{deviceNumber}/connected")
    override fun isConnected(@Path("deviceNumber") deviceNumber: Int): Call<AlpacaResponse<Boolean>>

    @FormUrlEncoded
    @PUT("telescope/{deviceNumber}/connected")
    override fun connect(@Path("deviceNumber") deviceNumber: Int, @Field("Connected") connected: Boolean): Call<AlpacaResponse<Any>>
}
