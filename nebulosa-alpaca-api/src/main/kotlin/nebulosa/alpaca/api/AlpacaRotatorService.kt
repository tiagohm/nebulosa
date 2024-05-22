package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface AlpacaRotatorService : AlpacaDeviceService {

    @GET("api/v1/rotator/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/rotator/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>

    @GET("api/v1/rotator/{id}/canreverse")
    fun canReverse(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/rotator/{id}/ismoving")
    fun isMoving(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/rotator/{id}/reverse")
    fun isReversed(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/rotator/{id}/position")
    fun position(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/rotator/{id}/stepsize")
    fun stepSize(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/rotator/{id}/reverse")
    fun reverse(@Path("id") id: Int, @Field("Reverse") reverse: Boolean): Call<NoneResponse>

    @PUT("api/v1/rotator/{id}/halt")
    fun halt(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/rotator/{id}/move")
    fun move(@Path("id") id: Int, @Field("Position") position: Double): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/rotator/{id}/moveabsolute")
    fun moveTo(@Path("id") id: Int, @Field("Position") position: Double): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/rotator/{id}/sync")
    fun sync(@Path("id") id: Int, @Field("Position") position: Double): Call<NoneResponse>
}
