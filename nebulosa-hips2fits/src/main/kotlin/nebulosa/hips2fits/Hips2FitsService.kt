package nebulosa.hips2fits

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.create

/**
 * The CDS hips2fits service offers a way to extract FITS images from HiPS sky maps.
 */
class Hips2FitsService(
    url: String = MAIN_URL,
    okHttpClient: OkHttpClient? = null,
) : RetrofitService(url, okHttpClient) {

    private val service by lazy { retrofit.create<Hips2Fits>() }

    /**
     * Extracts a FITS image from a HiPS given the output image pixel size,
     * the center of projection, the type of projection and the field of view.
     */
    fun query(
        hips: HipsSurvey,
        ra: Angle, dec: Angle,
        width: Int = 1200, height: Int = 900,
        rotation: Angle = Angle.ZERO,
        fov: Angle = DEFAULT_FOV,
        projection: ProjectionType = ProjectionType.TAN,
        coordSystem: CoordinateFrameType = CoordinateFrameType.ICRS,
        format: FormatOutputType = FormatOutputType.FITS,
    ): Call<ByteArray> {
        return service.query(
            hips.id, ra.degrees, dec.degrees, width, height, projection.name, fov.degrees,
            coordSystem.name.lowercase(), rotation.degrees, format.name.lowercase(),
        )
    }

    companion object {

        const val MAIN_URL = "https://alasky.u-strasbg.fr/"
        const val MIRROR_URL = "https://alaskybis.u-strasbg.fr/"

        @JvmStatic val DEFAULT_FOV = 0.5.deg
    }
}
