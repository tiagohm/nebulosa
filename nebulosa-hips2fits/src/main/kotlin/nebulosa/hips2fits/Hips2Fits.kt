package nebulosa.hips2fits

import nebulosa.retrofit.RawAsByteArray
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface Hips2Fits {

    @RawAsByteArray
    @GET("hips-image-services/hips2fits")
    fun query(
        @Query("hips") hips: String,
        @Query("ra") ra: Double, @Query("dec") dec: Double,
        @Query("width") width: Int, @Query("height") height: Int,
        @Query("projection") projection: String, @Query("fov") fov: Double,
        @Query("coordsys") coordSystem: String, @Query("rotation_angle") rotationAngle: Double,
        @Query("format") format: String,
    ): Call<ByteArray>
}
