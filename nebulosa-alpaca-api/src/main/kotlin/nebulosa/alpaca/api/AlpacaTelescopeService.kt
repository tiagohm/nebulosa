package nebulosa.alpaca.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant

interface AlpacaTelescopeService : AlpacaGuideOutputService {

    @GET("api/v1/telescope/{id}/connected")
    override fun isConnected(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/connected")
    override fun connect(@Path("id") id: Int, @Field("Connected") connected: Boolean): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/alignmentmode")
    fun alignmentMode(@Path("id") id: Int): Call<AlignmentModeResponse>

    @GET("api/v1/telescope/{id}/altitude")
    fun altitude(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/aperturearea")
    fun apertureArea(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/aperturediameter")
    fun apertureDiameter(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/athome")
    fun isAtHome(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/atpark")
    fun isAtPark(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/azimuth")
    fun azimuth(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/canfindhome")
    fun canFindHome(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canpark")
    fun canPark(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canpulseguide")
    override fun canPulseGuide(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansetdeclinationrate")
    fun canSetDeclinationRate(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansetguiderates")
    fun canSetGuideRates(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansetpark")
    fun canSetPark(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansetpierside")
    fun canSetPierSide(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansetrightascensionrate")
    fun canSetRightAscensionRate(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansettracking")
    fun canSetTracking(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canslew")
    fun canSlew(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canslewaltaz")
    fun canSlewAltAz(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canslewaltazasync")
    fun canSlewAltAzAsync(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canslewasync")
    fun canSlewAsync(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansync")
    fun canSync(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/cansyncaltaz")
    fun canSyncAltAz(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/canunpark")
    fun canUnpark(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/declination")
    fun declination(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/declinationrate")
    fun declinationRate(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/declinationrate")
    fun declinationRate(@Path("id") id: Int, @Field("DeclinationRate") rate: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/doesrefraction")
    fun doesRefraction(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/doesrefraction")
    fun doesRefraction(@Path("id") id: Int, @Field("DoesRefraction") doesRefraction: Boolean): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/equatorialsystem")
    fun equatorialSystem(@Path("id") id: Int): Call<EquatorialCoordinateTypeResponse>

    @GET("api/v1/telescope/{id}/focallength")
    fun focalLength(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/guideratedeclination")
    fun guideRateDeclination(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/guideratedeclination")
    fun guideRateDeclination(@Path("id") id: Int, @Field("GuideRateDeclination") rate: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/guideraterightascension")
    fun guideRateRightAscension(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/guideraterightascension")
    fun guideRateRightAscension(@Path("id") id: Int, @Field("GuideRateRightAscension") rate: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/ispulseguiding")
    override fun isPulseGuiding(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/rightascension")
    fun rightAscension(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/rightascensionrate")
    fun rightAscensionRate(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/rightascensionrate")
    fun rightAscensionRate(@Path("id") id: Int, @Field("RightAscensionRate") rate: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/sideofpier")
    fun sideOfPier(@Path("id") id: Int): Call<PierSideResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/sideofpier")
    fun sideofPier(@Path("id") id: Int, @Field("SideOfPier") side: PierSide): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/siderealtime")
    fun siderealTime(@Path("id") id: Int): Call<DoubleResponse>

    @GET("api/v1/telescope/{id}/siteelevation")
    fun siteElevation(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/siteelevation")
    fun siteElevation(@Path("id") id: Int, @Field("SiteElevation") elevation: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/sitelatitude")
    fun siteLatitude(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/sitelatitude")
    fun siteLatitude(@Path("id") id: Int, @Field("SiteLatitude") latitude: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/sitelongitude")
    fun siteLongitude(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/sitelongitude")
    fun siteLongitude(@Path("id") id: Int, @Field("SiteLongitude") longitude: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/slewing")
    fun isSlewing(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/slewsettletime")
    fun slewSettleTime(@Path("id") id: Int): Call<IntResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/slewsettletime")
    fun slewSettleTime(@Path("id") id: Int, @Field("SlewSettleTime") settleTime: Int): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/targetdeclination")
    fun targetDeclination(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/targetdeclination")
    fun targetDeclination(@Path("id") id: Int, @Field("TargetDeclination") declination: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/targetrightascension")
    fun targetRightAscension(@Path("id") id: Int): Call<DoubleResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/targetrightascension")
    fun targetRightAscension(@Path("id") id: Int, @Field("TargetRightAscension") rightAscension: Double): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/tracking")
    fun isTracking(@Path("id") id: Int): Call<BoolResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/tracking")
    fun tracking(@Path("id") id: Int, @Field("Tracking") tracking: Boolean): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/trackingrate")
    fun trackingRate(@Path("id") id: Int): Call<DriveRateResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/trackingrate")
    fun trackingRate(@Path("id") id: Int, @Field("TrackingRate") rate: DriveRate): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/trackingrates")
    fun trackingRates(@Path("id") id: Int): Call<ArrayResponse<DriveRate>>

    @GET("api/v1/telescope/{id}/utcdate")
    fun utcDate(@Path("id") id: Int): Call<DateTimeResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/utcDate")
    fun utcDate(@Path("id") id: Int, @Field("UTCDate") date: Instant): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/abortslew")
    fun abortSlew(@Path("id") id: Int): Call<NoneResponse>

    @GET("api/v1/telescope/{id}/axisrates")
    fun axisRates(@Path("id") id: Int, @Query("Axis") axis: AxisType): Call<ArrayResponse<AxisRate>>

    @GET("api/v1/telescope/{id}/canmoveaxis")
    fun canMoveAxis(@Path("id") id: Int): Call<BoolResponse>

    @GET("api/v1/telescope/{id}/destinationsideofpier")
    fun destinationSideOfPier(@Path("id") id: Int): Call<PierSideResponse>

    @PUT("api/v1/telescope/{id}/findhome")
    fun findHome(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/moveaxis")
    fun moveAxis(@Path("id") id: Int, @Field("Axis") axis: AxisType, @Field("Rate") rate: Double): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/park")
    fun park(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/pulseguide")
    override fun pulseGuide(
        @Path("id") id: Int,
        @Field("Direction") direction: PulseGuideDirection,
        @Field("Duration") durationInMilliseconds: Long,
    ): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/setpark")
    fun setPark(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/slewtoaltaz")
    fun slewToAltAz(@Path("id") id: Int, @Field("Azimuth") azimuth: Double, @Field("Altitude") altitude: Double): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/slewtoaltazasync")
    fun slewtoAltAzAsync(@Path("id") id: Int, @Field("Azimuth") azimuth: Double, @Field("Altitude") altitude: Double): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/slewtocoordinates")
    fun slewToCoordinates(
        @Path("id") id: Int,
        @Field("RightAscension") rightAscension: Double,
        @Field("Declination") declination: Double,
    ): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/slewtocoordinatesasync")
    fun slewToCoordinatesAsync(
        @Path("id") id: Int,
        @Field("RightAscension") rightAscension: Double,
        @Field("Declination") declination: Double,
    ): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/slewtotarget")
    fun slewToTarget(@Path("id") id: Int): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/slewtotargetasync")
    fun slewToTargetAsync(@Path("id") id: Int): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/synctoaltaz")
    fun syncToAltAz(@Path("id") id: Int, @Field("Azimuth") azimuth: Double, @Field("Altitude") altitude: Double): Call<NoneResponse>

    @FormUrlEncoded
    @PUT("api/v1/telescope/{id}/synctocoordinates")
    fun syncToCoordinates(
        @Path("id") id: Int,
        @Field("RightAscension") rightAscension: Double,
        @Field("Declination") declination: Double,
    ): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/synctotarget")
    fun syncToTarget(@Path("id") id: Int): Call<NoneResponse>

    @PUT("api/v1/telescope/{id}/unpark")
    fun unpark(@Path("id") id: Int): Call<NoneResponse>
}
