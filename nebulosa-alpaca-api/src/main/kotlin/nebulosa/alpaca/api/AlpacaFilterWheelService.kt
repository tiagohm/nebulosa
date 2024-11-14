package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlpacaFilterWheelService : AlpacaDeviceService {

    @GET("api/v1/filterwheel/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/filterwheel/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>

    @GET("api/v1/filterwheel/{id}/focusoffsets")
    fun focusOffsets(@Path("id") id: Int): Call<IntArrayResponse>

    @GET("api/v1/filterwheel/{id}/names")
    fun names(@Path("id") id: Int): Call<ListResponse<String>>

    @GET("api/v1/filterwheel/{id}/position")
    fun position(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/filterwheel/{id}/position")
    fun position(@Path("id") id: Int, @Field("Position") position: Int): Call<NoneResponse>
}
