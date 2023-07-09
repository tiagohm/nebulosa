package nebulosa.api.services

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.Location
import nebulosa.api.data.responses.BodyPositionResponse
import nebulosa.api.data.responses.TwilightResponse
import nebulosa.api.services.ephemeris.BodyEphemerisProvider
import nebulosa.api.services.ephemeris.HorizonsEphemerisProvider
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.position.GeographicPosition
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.hypot

@Service
@EnableScheduling
class AtlasService(
    private val horizonsEphemerisProvider: HorizonsEphemerisProvider,
    private val bodyEphemerisProvider: BodyEphemerisProvider,
    private val okHttpClient: OkHttpClient,
) {

    private val positions = HashMap<Location, GeographicPosition>()
    @Volatile private var sunImage = ByteArray(0)

    fun imageOfSun(output: HttpServletResponse) {
        output.contentType = "image/png"
        output.outputStream.write(sunImage)
    }

    fun positionOfSun(location: Location, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(SUN, location, dateTime)!!
    }

    fun positionOfMoon(location: Location, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(MOON, location, dateTime)!!
    }

    private fun positionOfBody(target: Any, location: Location, dateTime: LocalDateTime): BodyPositionResponse? {
        val elements = bodyEphemeris(target, location, dateTime)
        return elements.let { HorizonsElement.of(it, dateTime.minusMinutes(location.offsetInMinutes.toLong())) }
            ?.let(BodyPositionResponse::of)
    }

    private fun bodyEphemeris(target: Any, location: Location, dateTime: LocalDateTime): List<HorizonsElement> {
        return synchronized(target) {
            val position = positions.getOrPut(location, location::geographicPosition)
            val offsetInSeconds = location.offsetInMinutes * 60
            val zoneId = ZoneOffset.ofTotalSeconds(offsetInSeconds)
            if (target is Body) bodyEphemerisProvider.compute(target, position, dateTime, zoneId)
            else horizonsEphemerisProvider.compute(target, position, dateTime, zoneId)
        }
    }

    fun twilight(location: Location, date: LocalDate): TwilightResponse {
        val civilDusk = doubleArrayOf(0.0, 0.0)
        val nauticalDusk = doubleArrayOf(0.0, 0.0)
        val astronomicalDusk = doubleArrayOf(0.0, 0.0)
        val night = doubleArrayOf(0.0, 0.0)
        val astronomicalDawn = doubleArrayOf(0.0, 0.0)
        val nauticalDawn = doubleArrayOf(0.0, 0.0)
        val civilDawn = doubleArrayOf(0.0, 0.0)

        val ephemeris = bodyEphemeris(SUN, location, LocalDateTime.of(date, LocalTime.now()))
        val (a) = findDiscrete(0.0, (ephemeris.size - 1).toDouble(), TwilightDiscreteFunction(ephemeris), 1.0)

        civilDusk[0] = a[0] / 60.0
        civilDusk[1] = a[1] / 60.0
        nauticalDusk[0] = a[1] / 60.0
        nauticalDusk[1] = a[2] / 60.0
        astronomicalDusk[0] = a[2] / 60.0
        astronomicalDusk[1] = a[3] / 60.0
        night[0] = a[3] / 60.0
        night[1] = a[4] / 60.0
        astronomicalDawn[0] = a[4] / 60.0
        astronomicalDawn[1] = a[5] / 60.0
        nauticalDawn[0] = a[5] / 60.0
        nauticalDawn[1] = a[6] / 60.0
        civilDawn[0] = a[6] / 60.0
        civilDawn[1] = a[7] / 60.0

        return TwilightResponse(
            civilDusk, nauticalDusk, astronomicalDusk, night,
            astronomicalDawn, nauticalDawn, civilDawn,
        )
    }

    fun altitudePointsOfSun(location: Location, date: LocalDate): List<DoubleArray> {
        val ephemeris = bodyEphemeris(SUN, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris)
    }

    fun altitudePointsOfMoon(location: Location, date: LocalDate): List<DoubleArray> {
        val ephemeris = bodyEphemeris(MOON, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris)
    }

    private fun altitudePointsOfBody(ephemeris: List<HorizonsElement>): List<DoubleArray> {
        return (0..1440).map { doubleArrayOf(it.toDouble() / 60.0, ephemeris[it].asDouble(HorizonsQuantity.APPARENT_ALT)) }
    }

    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    private fun refreshImageOfSun() {
        val request = Request.Builder()
            .url(SUN_IMAGE_URL)
            .build()

        val image = okHttpClient.newCall(request)
            .execute()
            .body
            .use { ImageIO.read(it.byteStream()) }
            .removeBackground()

        val bytes = ByteArrayOutputStream(1024 * 128)
        ImageIO.write(image, "PNG", bytes)
        sunImage = bytes.toByteArray()
    }

    companion object {

        private const val SUN_IMAGE_URL = "https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIC.jpg"

        private const val SUN = "10"
        private const val MOON = "301"

        @JvmStatic
        private fun BufferedImage.removeBackground(): BufferedImage {
            val output = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            val centerX = width / 2.0
            val centerY = height / 2.0

            val sunRadius = (centerX * 0.92).toInt()
            val pixels = IntArray(5)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val distance = hypot(x - centerX, y - centerY)
                    val color = getRGB(x, y)

                    if (distance > sunRadius) {
                        val gray = ((color shr 16 and 0xff) + (color shr 8 and 0xff) + (color and 0xff)) / 3

                        if (gray >= 170) {
                            output.setRGB(x, y, color)
                        } else if (x > 1 && y > 1 && x < width - 1 && y < height - 1) {
                            pixels[1] = getRGB(x - 1, y - 1)
                            pixels[2] = getRGB(x + 1, y - 1)
                            pixels[3] = getRGB(x - 1, y + 1)
                            pixels[4] = getRGB(x + 1, y + 1)

                            // Blur (Anti-aliasing) the Sun edge.
                            val red = pixels.sumOf { it shr 16 and 0xff } / pixels.size
                            val green = pixels.sumOf { it shr 8 and 0xff } / pixels.size
                            val blue = pixels.sumOf { it and 0xff } / pixels.size

                            if (red >= 50) {
                                output.setRGB(x, y, 0xFF000000.toInt() + (red and 0xff shl 16) + (green and 0xff shl 8) + (blue and 0xff))
                            }
                        }
                    } else {
                        output.setRGB(x, y, color)
                    }
                }
            }

            return output
        }
    }
}
