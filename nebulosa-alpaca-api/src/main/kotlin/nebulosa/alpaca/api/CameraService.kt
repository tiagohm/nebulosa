package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface CameraService : DeviceService {

    @GET("camera/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("camera/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>
}
