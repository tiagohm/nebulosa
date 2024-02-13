package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface AlpacaTelescopeService : AlpacaDeviceService {

    @GET("api/v1/telescope/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>
}
