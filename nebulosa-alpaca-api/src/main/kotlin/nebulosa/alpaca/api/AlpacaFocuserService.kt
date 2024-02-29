package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.*

interface AlpacaFocuserService : AlpacaDeviceService {

    @GET("api/v1/focuser/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/focuser/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>

    @GET("api/v1/focuser/{id}/absolute")
    fun canAbsolute(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/focuser/{id}/ismoving")
    fun isMoving(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/focuser/{id}/maxincrement")
    fun maxIncrement(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/focuser/{id}/maxstep")
    fun maxStep(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/focuser/{id}/position")
    fun position(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/focuser/{id}/stepsize")
    fun stepSize(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/focuser/{id}/tempcomp")
    fun temperatureCompensation(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/focuser/{id}/tempcomp")
    fun temperatureCompensation(@Path("id") id: Int, @Field("TempComp") enabled: Boolean): Call<NoneResponse>

    @GET("api/v1/focuser/{id}/tempcompavailable")
    fun hasTemperatureCompensation(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/focuser/{id}/temperature")
    fun temperature(@Path("id") id: Int): Call<DoubleResponse>

    @PUT("api/v1/focuser/{id}/halt")
    fun halt(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/focuser/{id}/move")
    fun move(@Path("id") id: Int, @Field("Position") position: Int): Call<NoneResponse>
}
