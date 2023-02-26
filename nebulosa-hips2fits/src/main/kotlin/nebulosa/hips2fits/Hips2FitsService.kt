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

    fun hipsSurvey() = service.mocServerQuery("hips_service_url*=*alasky* && dataproduct_type=image", "record", "json")

    fun query(
        hips: HipsSurvey,
        ra: Angle, dec: Angle,
        width: Int = 1200, height: Int = 900,
        projection: ProjectionType = ProjectionType.TAN, fov: Angle = DEFAULT_FOV,
        coordSystem: CoordinateFrameType = CoordinateFrameType.ICRS, rotationAngle: Angle = Angle.ZERO,
        format: FormatOutputType = FormatOutputType.FITS,
    ): Call<ByteArray> {
        return service.query(
            hips.id, ra.degrees, dec.degrees, width, height, projection.name, fov.degrees,
            coordSystem.name.lowercase(), rotationAngle.degrees, format.name.lowercase(),
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

        @JvmStatic private val DEFAULT_FOV = 0.5.deg
    }
}
