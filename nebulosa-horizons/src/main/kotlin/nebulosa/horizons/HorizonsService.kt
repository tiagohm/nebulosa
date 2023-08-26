package nebulosa.horizons

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HorizonsService(
    url: String = "",
    okHttpClient: OkHttpClient? = null,
) : RetrofitService(url.ifBlank { URL }, okHttpClient) {

    override val converterFactory: List<Converter.Factory> = listOf(HorizonsEphemerisConverterFactory)

    private val service by lazy { retrofit.create<Horizons>() }

    fun observer(
        command: String,
        longitude: Angle, latitude: Angle, elevation: Distance = Distance.ZERO,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSize: Duration = DEFAULT_STEP_SIZE,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observer(
        wrap(command), wrap("${longitude.degrees},${latitude.degrees},${elevation.kilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSize.toMinutes()}m"),
        wrap(quantities.map { it.code }.toSortedSet().joinToString(",")),
        wrap(apparent), wrap(if (extraPrecision) "YES" else "NO"),
    )

    fun observerWithOsculationElements(
        name: String,
        epoch: String,
        eccentricity: String,
        longitudeOfAscendingNode: String,
        argumentOfPerihelion: String,
        inclination: String,
        perihelionDistance: String? = null,
        perihelionJulianDayNumber: String? = null,
        meanAnomaly: String? = null,
        semiMajorAxis: String? = null,
        meanMotion: String? = null,
        absoluteMagnitude: String? = null,
        longitude: Angle, latitude: Angle, elevation: Distance = Distance.ZERO,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSize: Duration = DEFAULT_STEP_SIZE,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observerWithOsculationElements(
        wrap(name), wrap(epoch), wrap(eccentricity), wrapNull(perihelionDistance),
        wrapNull(perihelionJulianDayNumber), wrap(longitudeOfAscendingNode),
        wrap(argumentOfPerihelion), wrap(inclination), wrapNull(meanAnomaly),
        wrapNull(semiMajorAxis), wrapNull(meanMotion), wrapNull(absoluteMagnitude),
        wrap("${longitude.degrees},${latitude.degrees},${elevation.kilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSize.toMinutes()}m"),
        wrap(quantities.map { it.code }.toSortedSet().joinToString(",")),
        wrap(apparent), wrap(if (extraPrecision) "YES" else "NO"),
    )

    fun observerWithTLE(
        tle: String,
        longitude: Angle, latitude: Angle, elevation: Distance = Distance.ZERO,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSize: Duration = DEFAULT_STEP_SIZE,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observerWithTLE(
        wrap(tle), wrap("${longitude.degrees},${latitude.degrees},${elevation.kilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSize.toMinutes()}m"),
        wrap(quantities.map { it.code }.toSortedSet().joinToString(",")),
        wrap(apparent), wrap(if (extraPrecision) "YES" else "NO"),
    )

    fun spk(id: Int, startTime: LocalDateTime, endTime: LocalDateTime): Call<SpkFile> {
        return service.spk("'DES=$id;'", wrap(startTime), wrap(endTime))
    }

    private object HorizonsEphemerisConverter : Converter<ResponseBody, HorizonsEphemeris> {

        override fun convert(value: ResponseBody): HorizonsEphemeris {
            return value.use { HorizonsEphemeris.parse(value.byteStream()) }
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

        const val URL = "https://ssd.jpl.nasa.gov/api/"

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        @JvmStatic private val DEFAULT_STEP_SIZE = Duration.ofMinutes(1L)

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrapNull(o: Any?) = if (o == null) null else "'$o'"

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(o: Any) = "'$o'"

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(dateTime: LocalDateTime) = "'${dateTime.format(DATE_TIME_FORMAT)}'"
    }
}
