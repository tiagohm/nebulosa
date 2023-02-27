package nebulosa.hips2fits

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class Hips2FitsService(url: String = MAIN_URL) : RetrofitService(url) {

    override val converterFactory: List<Converter.Factory> = listOf(ByteArrayConverterFactory)

    private val service by lazy { retrofit.create(Hips2Fits::class.java) }

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

    private object ByteArrayConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ) = if (type == ByteArray::class.java) ByteArrayConverter else null
    }

    companion object {

        const val MAIN_URL = "https://alasky.u-strasbg.fr/"
        const val MIRROR_URL = "https://alaskybis.u-strasbg.fr/"

        @JvmStatic val DEFAULT_FOV = 0.5.deg
    }
}
