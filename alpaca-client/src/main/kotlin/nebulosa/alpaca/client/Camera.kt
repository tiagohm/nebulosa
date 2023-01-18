package nebulosa.alpaca.client

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface Camera : Device {

    @GET("camera/{deviceNumber}/connected")
    override fun isConnected(@Path("deviceNumber") deviceNumber: Int): AlpacaResponse<Boolean>

    @FormUrlEncoded
    @PUT("camera/{deviceNumber}/connected")
    override fun connect(@Path("deviceNumber") deviceNumber: Int, @Field("Connected") connected: Boolean): AlpacaResponse<Any>
}
