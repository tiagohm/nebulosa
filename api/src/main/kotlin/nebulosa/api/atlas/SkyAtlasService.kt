package nebulosa.api.atlas

import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import nebulosa.math.toLightYears
import nebulosa.math.toMas
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.ELPMPP02
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.GeographicCoordinate
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.TimeZonedInSeconds
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.hypot

@Service
@EnableScheduling
class SkyAtlasService(
    private val horizonsEphemerisProvider: HorizonsEphemerisProvider,
    private val bodyEphemerisProvider: BodyEphemerisProvider,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val satelliteRepository: SatelliteRepository,
    private val simbadEntityRepository: SimbadEntityRepository,
    private val httpClient: OkHttpClient,
) {

    private val positions = HashMap<GeographicCoordinate, GeographicPosition>()
    private val cachedSimbadEntities = HashMap<Long, SimbadEntity>()
    private val targetLocks = HashMap<Any, Any>()
    @Volatile private var sunImage = ByteArray(0)

    val objectTypes: Collection<SkyObjectType> by lazy { simbadEntityRepository.findAll().map { it.type }.toSortedSet() }

    fun imageOfSun(output: HttpServletResponse) {
        output.contentType = "image/png"
        output.outputStream.write(sunImage)
    }

    fun positionOfSun(location: GeographicCoordinate, dateTime: LocalDateTime, fast: Boolean = false): BodyPosition {
        return positionOfBody(if (fast) VSOP87E.SUN else SUN, location, dateTime)!!
    }

    fun positionOfMoon(location: GeographicCoordinate, dateTime: LocalDateTime, fast: Boolean = false): BodyPosition {
        return positionOfBody(if (fast) FAST_MOON else MOON, location, dateTime)!!
    }

    fun positionOfPlanet(location: GeographicCoordinate, code: String, dateTime: LocalDateTime, fast: Boolean = false): BodyPosition {
        val target: Any = VSOP87E.entries.takeIf { fast }?.find { "${it.target}" == code } ?: code
        return positionOfBody(target, location, dateTime)!!
    }

    fun positionOfSkyObject(location: GeographicCoordinate, id: Long, dateTime: LocalDateTime): BodyPosition {
        val target = cachedSimbadEntities[id] ?: simbadEntityRepository.find(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found sky object: [$id]")
        cachedSimbadEntities[id] = target
        val distance = SkyObject.distanceFor(target.parallax.toMas)
        return positionOfBody(target, location, dateTime)!!
            .copy(magnitude = target.magnitude, constellation = target.constellation, distance = distance.toLightYears, distanceUnit = "ly")
    }

    fun positionOfSatellite(location: GeographicCoordinate, satellite: SatelliteEntity, dateTime: LocalDateTime): BodyPosition {
        return positionOfBody("TLE@${satellite.tle}", location, dateTime)!!
    }

    private fun positionOfBody(target: Any, location: GeographicCoordinate, dateTime: LocalDateTime): BodyPosition? {
        return bodyEphemeris(target, location, dateTime, false)
            .withLocationAndDateTime(location, dateTime)
            ?.let(BodyPosition::of)
    }

    private fun bodyEphemeris(
        target: Any, location: GeographicCoordinate,
        dateTime: LocalDateTime, fully: Boolean,
    ): List<HorizonsElement> {
        val position = synchronized(positions) {
            if (location is Location || location is Mount) positions.getOrPut(location) { location.geographicPosition()!! }
            else location.geographicPosition()
        } ?: return emptyList()

        val lock = synchronized(targetLocks) { targetLocks.getOrPut(target) { Any() } }

        return synchronized(lock) {
            val offsetInSeconds = location.offsetInSeconds().toLong()
            if (target is Body) bodyEphemerisProvider.compute(target, position, dateTime, offsetInSeconds, fully)
            else horizonsEphemerisProvider.compute(target, position, dateTime, offsetInSeconds, fully)
        }
    }

    fun searchSatellites(text: String, groups: List<SatelliteGroupType>): List<SatelliteEntity> {
        return satelliteRepository.search(text.ifBlank { null }, groups)
    }

    fun twilight(location: GeographicCoordinate, date: LocalDate, fast: Boolean = false): Twilight {
        val civilDusk = doubleArrayOf(0.0, 0.0)
        val nauticalDusk = doubleArrayOf(0.0, 0.0)
        val astronomicalDusk = doubleArrayOf(0.0, 0.0)
        val night = doubleArrayOf(0.0, 0.0)
        val astronomicalDawn = doubleArrayOf(0.0, 0.0)
        val nauticalDawn = doubleArrayOf(0.0, 0.0)
        val civilDawn = doubleArrayOf(0.0, 0.0)

        val ephemeris = bodyEphemeris(if (fast) VSOP87E.SUN else SUN, location, LocalDateTime.of(date, LocalTime.now()), true)
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

    fun altitudePointsOfSun(location: GeographicCoordinate, date: LocalDate, stepSize: Int, fast: Boolean = false): List<DoubleArray> {
        val ephemeris = bodyEphemeris(if (fast) VSOP87E.SUN else SUN, location, LocalDateTime.of(date, LocalTime.now()), true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfMoon(location: GeographicCoordinate, date: LocalDate, stepSize: Int, fast: Boolean = false): List<DoubleArray> {
        val ephemeris = bodyEphemeris(if (fast) FAST_MOON else MOON, location, LocalDateTime.of(date, LocalTime.now()), true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfPlanet(
        location: GeographicCoordinate, code: String, date: LocalDate,
        stepSize: Int, fast: Boolean = false
    ): List<DoubleArray> {
        val target: Any = VSOP87E.entries.takeIf { fast }?.find { "${it.target}" == code } ?: code
        val ephemeris = bodyEphemeris(target, location, LocalDateTime.of(date, LocalTime.now()), true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfSkyObject(location: GeographicCoordinate, id: Long, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val target = cachedSimbadEntities[id] ?: simbadEntityRepository.find(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found sky object: [$id]")
        cachedSimbadEntities[id] = target
        val ephemeris = bodyEphemeris(target, location, LocalDateTime.of(date, LocalTime.now()), true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfSatellite(location: GeographicCoordinate, satellite: SatelliteEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris("TLE@${satellite.tle}", location, LocalDateTime.of(date, LocalTime.now()), true)
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

    fun closeApproachesForMinorPlanets(days: Long, distance: Double, date: LocalDate?) = smallBodyDatabaseService
        .closeApproaches(days, distance, date).execute().body()
        ?.let(CloseApproach::of)
        ?: emptyList()

    fun searchSkyObject(
        text: String? = null,
        rightAscension: Angle = 0.0, declination: Angle = 0.0, radius: Angle = 0.0,
        constellation: Constellation? = null,
        magnitudeMin: Double = SkyObject.MAGNITUDE_MIN, magnitudeMax: Double = SkyObject.MAGNITUDE_MAX,
        type: SkyObjectType? = null,
    ) = simbadEntityRepository.find(text, constellation, rightAscension, declination, radius, magnitudeMin, magnitudeMax, type)

    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    fun refreshImageOfSun() {
        val request = Request.Builder()
            .url(SUN_IMAGE_URL)
            .build()

        val image = httpClient.newCall(request)
            .execute()
            .body
            .use { ImageIO.read(it!!.byteStream()) }
            .removeBackground()

        val bytes = ByteArrayOutputStream(1024 * 128)
        ImageIO.write(image, "PNG", bytes)
        sunImage = bytes.toByteArray()
    }

    companion object {

        private const val SUN_IMAGE_URL = "https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIC.jpg"

        private const val SUN = "10"
        private const val MOON = "301"

        @JvmStatic private val FAST_MOON = VSOP87E.EARTH + ELPMPP02

        @JvmStatic
        private fun GeographicCoordinate.geographicPosition() = when (this) {
            is GeographicPosition -> this
            is Location -> Geoid.IERS2010.lonLat(this)
            else -> null
        }

        @JvmStatic
        private fun GeographicCoordinate.offsetInSeconds() = when (this) {
            is TimeZonedInSeconds -> offsetInSeconds
            else -> 0
        }

        @JvmStatic
        private fun GeographicCoordinate.offsetInMinutes() = when (this) {
            is Location -> offsetInMinutes
            is TimeZonedInSeconds -> offsetInSeconds / 60
            else -> 0
        }

        @JvmStatic
        private fun Double.clampMagnitude(): Double {
            return if (this in SkyObject.MAGNITUDE_RANGE) this
            else if (this < SkyObject.MAGNITUDE_MIN) SkyObject.MAGNITUDE_MIN
            else SkyObject.MAGNITUDE_MAX
        }

        @JvmStatic
        private fun List<HorizonsElement>.withLocationAndDateTime(location: GeographicCoordinate, dateTime: LocalDateTime): HorizonsElement? {
            val offsetInMinutes = location.offsetInMinutes().toLong()
            return let { HorizonsElement.of(it, dateTime.minusMinutes(offsetInMinutes)) }
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
