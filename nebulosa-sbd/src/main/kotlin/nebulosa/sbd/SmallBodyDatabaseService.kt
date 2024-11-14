package nebulosa.sbd

import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.math.deg
import nebulosa.math.format
import nebulosa.math.m
import nebulosa.math.toDegrees
import nebulosa.math.toKilometers
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.create
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SmallBodyDatabaseService(
    url: String = "https://ssd-api.jpl.nasa.gov/",
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    private val service by lazy { retrofit.create<SmallBodyDatabase>() }

    override fun withOkHttpClient(builder: OkHttpClient.Builder) {
        builder.readTimeout(5L, TimeUnit.MINUTES)
            .writeTimeout(5L, TimeUnit.MINUTES)
            .connectTimeout(5L, TimeUnit.MINUTES)
            .callTimeout(5L, TimeUnit.MINUTES)
            .addInterceptor {
                val response = it.proceed(it.request())

                if (response.code == 300) {
                    response.newBuilder()
                        .code(200)
                        .build()
                } else {
                    response
                }
            }
    }

    fun search(text: String) = service.search(text)

    fun identify(
        dateTime: LocalDateTime,
        latitude: Angle, longitude: Angle, elevation: Distance = 0.m,
        centerRA: Angle, centerDEC: Angle, fov: Angle = DEFAULT_FOV,
        magLimit: Double = 18.0, magRequired: Boolean = true,
    ) = service.identify(
        dateTime.format(DATE_TIME_FORMAT),
        latitude.toDegrees, longitude.toDegrees, elevation.toKilometers,
        centerRA.format(RA_FORMAT), centerDEC.format(DEC_FORMAT), fovRAWidth = fov.toDegrees,
        magLimit = magLimit, magRequired = magRequired && magLimit < 30.0
    )

    fun closeApproaches(
        days: Long = 7L, distance: Double = 2.0, date: LocalDate? = null,
    ) = service.closeApproaches(date?.toString() ?: "now", date?.plusDays(days)?.toString() ?: "+$days", "${distance}LD")

    companion object {

        private val DEFAULT_FOV = 1.0.deg
        private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")

        private val RA_FORMAT = AngleFormatter.Builder()
            .hours()
            .separators("-")
            .minusSign("M")
            .plusSign("")
            .secondsDecimalPlaces(2)
            .build()

        private val DEC_FORMAT = AngleFormatter.Builder()
            .separators("-")
            .degreesFormat("%02d")
            .minusSign("M")
            .plusSign("")
            .secondsDecimalPlaces(2)
            .build()
    }
}
