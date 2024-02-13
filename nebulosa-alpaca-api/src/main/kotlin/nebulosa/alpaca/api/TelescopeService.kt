package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface TelescopeService : DeviceService {

    @GET("telescope/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("telescope/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>
}
