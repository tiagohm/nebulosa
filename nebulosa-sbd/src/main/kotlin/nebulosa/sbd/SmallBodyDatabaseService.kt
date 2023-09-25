package nebulosa.sbd

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.create
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SmallBodyDatabaseService(
    url: String = "https://ssd-api.jpl.nasa.gov/",
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    private val service by lazy { retrofit.create<SmallBodyDatabase>() }

    override fun handleOkHttpClientBuilder(builder: OkHttpClient.Builder) {
        builder.addInterceptor {
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
        centerRA: Angle, centerDEC: Angle, fov: Angle = 1.0.deg,
        magLimit: Double = 12.0,
    ): Call<SmallBodyIdentified> {
        val fovDeg = fov.degrees

        return service.identify(
            dateTime.format(DATE_TIME_FORMAT),
            latitude.degrees, longitude.degrees, elevation.kilometers,
            centerRA.format(RA_FORMAT), centerDEC.format(DEC_FORMAT), fovDeg, fovDeg,
            magLimit,
        )
    }

    companion object {

        @JvmStatic private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")

        @JvmStatic private val RA_FORMAT = AngleFormatter.Builder()
            .hours()
            .separators("-")
            .minusSign("M")
            .plusSign("")
            .secondsDecimalPlaces(2)
            .build()

        @JvmStatic private val DEC_FORMAT = AngleFormatter.Builder()
            .separators("-")
            .degreesFormat("%02d")
            .minusSign("M")
            .plusSign("")
            .secondsDecimalPlaces(2)
            .build()
    }
}
