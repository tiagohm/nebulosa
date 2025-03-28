package nebulosa.alpaca.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlpacaCameraService : AlpacaGuideOutputService {

    @GET("api/v1/camera/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>

    @GET("api/v1/camera/{id}/driverversion")
    override fun driverVersion(@Path("id") id: Int): Call<StringResponse>

    @GET("api/v1/camera/{id}/driverinfo")
    override fun driverInfo(@Path("id") id: Int): Call<StringResponse>

    @GET("api/v1/camera/{id}/bayeroffsetx")
    fun bayerOffsetX(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/bayeroffsety")
    fun bayerOffsetY(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/binx")
    fun binX(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/binx")
    fun binX(@Path("id") id: Int, @Field("BinX") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/biny")
    fun binY(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/biny")
    fun binY(@Path("id") id: Int, @Field("BinY") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/camerastate")
    fun cameraState(@Path("id") id: Int): Call<CameraStateResponse>

    @GET("api/v1/camera/{id}/cameraxsize")
    fun x(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/cameraysize")
    fun y(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/canabortexposure")
    fun canAbortExposure(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/canasymmetricbin")
    fun canAsymmetricBin(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/canfastreadout")
    fun canFastReadout(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/cangetcoolerpower")
    fun canCoolerPower(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/canpulseguide")
    override fun canPulseGuide(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/cansetccdtemperature")
    fun canSetCCDTemperature(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/canstopexposure")
    fun canStopExposure(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/ccdtemperature")
    fun ccdTemperature(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/cooleron")
    fun isCoolerOn(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/cooleron")
    fun cooler(@Path("id") id: Int, @Field("CoolerOn") value: Boolean): Call<NoneResponse>

    @GET("api/v1/camera/{id}/coolerpower")
    fun coolerPower(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/electronsperadu")
    fun electronsPerADU(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/exposuremax")
    fun exposureMax(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/exposuremin")
    fun exposureMin(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/exposureresolution")
    fun exposureResolution(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/fastreadout")
    fun isFastReadout(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/fastreadout")
    fun fastReadout(@Path("id") id: Int, @Field("FastReadout") value: Boolean): Call<NoneResponse>

    @GET("api/v1/camera/{id}/fullwellcapacity")
    fun fullWellCapacity(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/gain")
    fun gain(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/gain")
    fun gain(@Path("id") id: Int, @Field("Gain") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/gainmax")
    fun gainMax(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/gainmin")
    fun gainMin(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/gains")
    fun gains(@Path("id") id: Int): Call<ArrayResponse<String>>

    @GET("api/v1/camera/{id}/hasshutter")
    fun hasShutter(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/heatsinktemperature")
    fun heatSinkTemperature(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/imageready")
    fun isImageReady(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/ispulseguiding")
    override fun isPulseGuiding(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/camera/{id}/lastexposureduration")
    fun lastExposureDuration(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/lastexposurestarttime")
    fun lastExposureStartTime(@Path("id") id: Int): Call<StringResponse>

    @GET("api/v1/camera/{id}/maxadu")
    fun maxADU(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/maxbinx")
    fun maxBinX(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/maxbiny")
    fun maxBinY(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/numx")
    fun numX(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/numx")
    fun numX(@Path("id") id: Int, @Field("NumX") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/numy")
    fun numY(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/numy")
    fun numY(@Path("id") id: Int, @Field("NumY") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/offset")
    fun offset(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/offset")
    fun offset(@Path("id") id: Int, @Field("Offset") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/offsetmax")
    fun offsetMax(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/offsetmin")
    fun offsetMin(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/offsets")
    fun offsets(@Path("id") id: Int): Call<ArrayResponse<String>>

    @GET("api/v1/camera/{id}/percentcompleted")
    fun percentCompleted(@Path("id") id: Int): Call<IntResponse>

    @GET("api/v1/camera/{id}/pixelsizex")
    fun pixelSizeX(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/pixelsizey")
    fun pixelSizeY(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/camera/{id}/readoutmode")
    fun readoutMode(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/readoutmode")
    fun readoutMode(@Path("id") id: Int, @Field("ReadoutMode") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/readoutmodes")
    fun readoutModes(@Path("id") id: Int): Call<ArrayResponse<String>>

    @GET("api/v1/camera/{id}/sensorname")
    fun sensorName(@Path("id") id: Int): Call<StringResponse>

    @GET("api/v1/camera/{id}/sensortype")
    fun sensorType(@Path("id") id: Int): Call<SensorTypeResponse>

    @GET("api/v1/camera/{id}/setccdtemperature")
    fun setpointCCDTemperature(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/setccdtemperature")
    fun setpointCCDTemperature(@Path("id") id: Int, @Field("SetCCDTemperature") value: Double): Call<NoneResponse>

    @GET("api/v1/camera/{id}/startx")
    fun startX(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/startx")
    fun startX(@Path("id") id: Int, @Field("StartX") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/starty")
    fun startY(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/starty")
    fun startY(@Path("id") id: Int, @Field("StartY") value: Int): Call<NoneResponse>

    @GET("api/v1/camera/{id}/subexposureduration")
    fun subExposureDuration(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/subexposureduration")
    fun subExposureDuration(@Path("id") id: Int, @Field("SubExposureDuration") value: Double): Call<NoneResponse>

    @PUT("api/v1/camera/{id}/abortexposure")
    fun abortExposure(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/pulseguide")
    override fun pulseGuide(
        @Path("id") id: Int,
        @Field("Direction") direction: PulseGuideDirection,
        @Field("Duration") durationInMilliseconds: Long,
    ): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/camera/{id}/startexposure")
    fun startExposure(@Path("id") id: Int, @Field("Duration") durationInSeconds: Double, @Field("Light") light: Boolean): Call<NoneResponse>

    @PUT("api/v1/camera/{id}/stopexposure")
    fun stopExposure(@Path("id") id: Int): Call<NoneResponse>

    // https://github.com/ASCOMInitiative/ASCOMRemote/blob/main/Documentation/AlpacaImageBytes.pdf
    @Headers("Accept: application/imagebytes")
    @GET("api/v1/camera/{id}/imagearray")
    fun imageArray(@Path("id") id: Int): Call<ResponseBody>
}
