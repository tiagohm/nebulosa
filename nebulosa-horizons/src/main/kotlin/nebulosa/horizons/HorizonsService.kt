package nebulosa.horizons

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.toDegrees
import nebulosa.math.toKilometers
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HorizonsService(
    url: String = "",
    httpClient: OkHttpClient? = null,
) : RetrofitService(url.ifBlank { URL }, httpClient) {

    override val converterFactory: List<Converter.Factory> = listOf(HorizonsEphemerisConverterFactory)

    private val service by lazy { retrofit.create<Horizons>() }

    fun observer(
        command: String,
        longitude: Angle, latitude: Angle, elevation: Distance = 0.0,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSizeInMinutes: Int = 1,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observer(
        wrap(command), wrap("${longitude.toDegrees},${latitude.toDegrees},${elevation.toKilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSizeInMinutes}m"),
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
        longitude: Angle, latitude: Angle, elevation: Distance = 0.0,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSizeInMinutes: Int = 1,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observerWithOsculationElements(
        wrap(name), wrap(epoch), wrap(eccentricity), wrapNull(perihelionDistance),
        wrapNull(perihelionJulianDayNumber), wrap(longitudeOfAscendingNode),
        wrap(argumentOfPerihelion), wrap(inclination), wrapNull(meanAnomaly),
        wrapNull(semiMajorAxis), wrapNull(meanMotion), wrapNull(absoluteMagnitude),
        wrap("${longitude.toDegrees},${latitude.toDegrees},${elevation.toKilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSizeInMinutes}m"),
        wrap(quantities.map { it.code }.toSortedSet().joinToString(",")),
        wrap(apparent), wrap(if (extraPrecision) "YES" else "NO"),
    )

    fun observerWithTLE(
        tle: String,
        longitude: Angle, latitude: Angle, elevation: Distance = 0.0,
        startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusDays(1L),
        stepSizeInMinutes: Int = 1,
        apparent: ApparentRefractionCorrection = ApparentRefractionCorrection.AIRLESS,
        extraPrecision: Boolean = false,
        vararg quantities: HorizonsQuantity = HorizonsQuantity.ENTRIES,
    ) = service.observerWithTLE(
        wrap(tle), wrap("${longitude.toDegrees},${latitude.toDegrees},${elevation.toKilometers}"),
        wrap(startTime), wrap(endTime), wrap("${stepSizeInMinutes}m"),
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

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrapNull(o: Any?) = if (o == null) null else "'$o'"

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(o: Any) = "'$o'"

        @Suppress("NOTHING_TO_INLINE")
        private inline fun wrap(dateTime: LocalDateTime) = "'${dateTime.format(DATE_TIME_FORMAT)}'"
    }
}
