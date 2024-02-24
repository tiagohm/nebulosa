package nebulosa.hips2fits

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface Hips2Fits {

    @GET("hips-image-services/hips2fits")
    fun query(
        @Query("hips") hips: String,
        @Query("ra") ra: Double, @Query("dec") dec: Double,
        @Query("width") width: Int, @Query("height") height: Int,
        @Query("projection") projection: String, @Query("fov") fov: Double,
        @Query("coordsys") coordSystem: String, @Query("rotation_angle") rotationAngle: Double,
        @Query("format") format: String,
    ): Call<ResponseBody>

    /**
     * MOC Server tool for retrieving as fast as possible the list of astronomical data sets
     * (35531 catalogs, surveys, ... harvested from CDS and VO servers) having at least
     * one observation in a specifical sky region/and or time range.
     *
     * The default result is an ID list. MOC Server is based on Multi-Order Coverage maps (MOC)
     * described in the IVOA REC. standard.
     *
     * Query example: `hips_service_url*=*alasky* && dataproduct_type=image && moc_sky_fraction >= 0.99`
     *
     * @see <a href="https://alasky.u-strasbg.fr/MocServer/query">Web page</a>
     */
    @GET("MocServer/query?get=record&fmt=json")
    fun availableSurveys(@Query("expr") expr: String): Call<List<HipsSurvey>>
}
