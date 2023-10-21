package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.api.locations.LocationEntity
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.math.Angle
import nebulosa.math.toLightYears
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.data.domain.Pageable
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
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val satelliteRepository: SatelliteRepository,
    private val httpClient: OkHttpClient,
) {

    private val positions = HashMap<LocationEntity, GeographicPosition>()
    @Volatile private var sunImage = ByteArray(0)

    val starTypes by lazy { starRepository.types() }

    val dsoTypes by lazy { deepSkyObjectRepository.types() }

    fun imageOfSun(output: HttpServletResponse) {
        output.contentType = "image/png"
        output.outputStream.write(sunImage)
    }

    fun positionOfSun(location: LocationEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody(SUN, location, dateTime)!!
    }

    fun positionOfMoon(location: LocationEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody(MOON, location, dateTime)!!
    }

    fun positionOfPlanet(location: LocationEntity, code: String, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody(code, location, dateTime)!!
    }

    fun positionOfStar(location: LocationEntity, star: StarEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody(star, location, dateTime)!!
            .copy(magnitude = star.magnitude, constellation = star.constellation, distance = star.distance.toLightYears, distanceUnit = "ly")
    }

    fun positionOfDSO(location: LocationEntity, dso: DeepSkyObjectEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody(dso, location, dateTime)!!
            .copy(magnitude = dso.magnitude, constellation = dso.constellation, distance = dso.distance.toLightYears, distanceUnit = "ly")
    }

    fun positionOfSatellite(location: LocationEntity, satellite: SatelliteEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody("TLE@${satellite.tle}", location, dateTime)!!
    }

    private fun positionOfBody(target: Any, location: LocationEntity, dateTime: LocalDateTime): BodyPosition? {
        return bodyEphemeris(target, location, dateTime)
            .withLocationAndDateTime(location, dateTime)
            ?.let(BodyPosition::of)
    }

    private fun bodyEphemeris(target: Any, location: LocationEntity, dateTime: LocalDateTime): List<HorizonsElement> {
        val position = positions.getOrPut(location, location::geographicPosition)
        val offsetInSeconds = location.offsetInMinutes * 60
        val zoneId = ZoneOffset.ofTotalSeconds(offsetInSeconds)
        return if (target is Body) bodyEphemerisProvider.compute(target, position, dateTime, zoneId)
        else horizonsEphemerisProvider.compute(target, position, dateTime, zoneId)
    }

    fun searchSatellites(text: String, groups: List<SatelliteGroupType>): List<SatelliteEntity> {
        return satelliteRepository.search(text.ifBlank { null }, groups, Pageable.ofSize(1000))
    }

    fun twilight(location: LocationEntity, date: LocalDate): Twilight {
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

        return Twilight(
            civilDusk, nauticalDusk, astronomicalDusk, night,
            astronomicalDawn, nauticalDawn, civilDawn,
        )
    }

    fun altitudePointsOfSun(location: LocationEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(SUN, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfMoon(location: LocationEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(MOON, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfPlanet(location: LocationEntity, code: String, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(code, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfStar(location: LocationEntity, star: StarEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(star, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfDSO(location: LocationEntity, dso: DeepSkyObjectEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(dso, location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfSatellite(location: LocationEntity, satellite: SatelliteEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris("TLE@${satellite.tle}", location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    private fun altitudePointsOfBody(ephemeris: List<HorizonsElement>, stepSize: Int): List<DoubleArray> {
        val points = ArrayList<DoubleArray>(1441)

        for (i in 0..1440 step stepSize) {
            val altitude = ephemeris[i].asDouble(HorizonsQuantity.APPARENT_ALT)
            points.add(doubleArrayOf(i / 60.0, altitude))
        }

        return points
    }

    fun searchMinorPlanet(text: String) = smallBodyDatabaseService
        .search(text).execute().body()
        ?.let(MinorPlanet::of)
        ?: MinorPlanet.EMPTY

    fun searchStar(
        text: String,
        rightAscension: Angle = 0.0, declination: Angle = 0.0, radius: Angle = 0.0,
        constellation: Constellation? = null,
        magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE, magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
        type: SkyObjectType? = null,
    ) = starRepository.search(
        text.replace(INVALID_DSO_CHARS, "").replace("][", ""),
        rightAscension, declination, radius,
        constellation,
        magnitudeMin.clampMagnitude(), magnitudeMax.clampMagnitude(), type,
        Pageable.ofSize(5000),
    )

    fun searchDSO(
        text: String,
        rightAscension: Angle = 0.0, declination: Angle = 0.0, radius: Angle = 0.0,
        constellation: Constellation? = null,
        magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE, magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
        type: SkyObjectType? = null,
    ) = deepSkyObjectRepository.search(
        text.replace(INVALID_DSO_CHARS, "").replace("][", ""),
        rightAscension, declination, radius,
        constellation,
        magnitudeMin.clampMagnitude(), magnitudeMax.clampMagnitude(), type,
        Pageable.ofSize(5000),
    )

    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    private fun refreshImageOfSun() {
        val request = Request.Builder()
            .url(SUN_IMAGE_URL)
            .build()

        val image = httpClient.newCall(request)
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

        @JvmStatic private val INVALID_DSO_CHARS = Regex("[^\\w\\-\\s\\[\\].+]+")

        @JvmStatic
        private fun Double.clampMagnitude(): Double {
            return if (this in -29.9..29.9) this
            else if (this < 0.0) -SkyObject.UNKNOWN_MAGNITUDE
            else SkyObject.UNKNOWN_MAGNITUDE
        }

        @JvmStatic
        private fun List<HorizonsElement>.withLocationAndDateTime(location: LocationEntity, dateTime: LocalDateTime): HorizonsElement? {
            return let { HorizonsElement.of(it, dateTime.minusMinutes(location.offsetInMinutes.toLong())) }
        }

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
