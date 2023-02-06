package nebulosa.query.horizons

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.query.QueryService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HorizonsService(url: String = "https://ssd.jpl.nasa.gov/api/") :
    QueryService(url, converterFactory = HorizonsEphemerisConverterFactory), Horizons {

    private val service = retrofit.create(Horizons::class.java)

    override fun observer(
        command: String, coordinates: String, startTime: String,
        endTime: String, stepSize: String, quantities: String,
        apparent: String, extraPrecision: String,
    ): Call<HorizonsEphemeris> {
        return service.observer(command, coordinates, startTime, endTime, stepSize, quantities, apparent, extraPrecision)
    }

    fun observer(
        command: String,
        longitude: Angle, latitude: Angle, elevation: Distance = Distance.ZERO,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSize: Duration = DEFAULT_STEP_SIZE,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ): Call<HorizonsEphemeris> {
        return service.observer(
            command, wrap("${longitude.degrees},${latitude.degrees},${elevation.kilometers}"),
            wrap(startTime), wrap(endTime), wrap(stepSize.seconds),
            wrap(quantities.map { it.code }.toSet().joinToString(",")),
            wrap(apparent), wrap(if (extraPrecision) "YES" else "NO"),
        )
    }

    override fun spk(command: String, startTime: String, endTime: String): Call<SpkFile> {
        return service.spk(command, startTime, endTime)
    }

    fun spk(id: Int, startTime: LocalDateTime, endTime: LocalDateTime): Call<SpkFile> {
        return spk("'DES=$id;'", wrap(startTime), wrap(endTime))
    }

    private object HorizonsEphemerisConverter : Converter<ResponseBody, HorizonsEphemeris> {

        override fun convert(value: ResponseBody): HorizonsEphemeris {
            return HorizonsEphemeris.parse(value.byteStream())
        }
    }

    private object HorizonsEphemerisConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<ResponseBody, *>? {
            return if (type === HorizonsEphemeris::class.java) HorizonsEphemerisConverter
            else null
        }
    }

    companion object {

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        @JvmStatic private val DEFAULT_STEP_SIZE = Duration.ofMinutes(1L)

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(o: Any) = "'$o'"

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(dateTime: LocalDateTime) = "'${dateTime.format(DATE_TIME_FORMAT)}'"
    }
}
