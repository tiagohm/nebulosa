package nebulosa.api.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.math.Angle
import nebulosa.math.evenlySpacedNumbers
import nebulosa.math.toLightYears
import nebulosa.math.toMas
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.almanac.lunation
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
import nebulosa.time.TimeYMDHMS
import nebulosa.time.TimeZonedInSeconds
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.hypot

class SkyAtlasService(
    private val horizonsEphemerisProvider: HorizonsEphemerisProvider,
    private val bodyEphemerisProvider: BodyEphemerisProvider,
    private val smallBodyDatabaseService: SmallBodyDatabaseService,
    private val satelliteRepository: SatelliteRepository,
    private val skyObjectEntityRepository: SkyObjectEntityRepository,
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val moonPhaseFinder: MoonPhaseFinder,
    private val earthSeasonFinder: EarthSeasonFinder,
    scheduledExecutorService: ScheduledExecutorService,
) {

    private val positions = HashMap<GeographicCoordinate, GeographicPosition>()
    private val cachedSkyObjectEntities = HashMap<Long, SkyObjectEntity>()
    private val targetLocks = HashMap<Any, Any>()
    private val moonPhaseDateTime = HashMap<Pair<LocalDate, Boolean>, List<MoonPhaseDateTime>>()
    private val earthSeasonDateTime = HashMap<Int, List<EarthSeasonDateTime>>()
    private val moonPhaseInfo = HashMap<LocalDateTime, MoonPhaseInfo>()

    @Volatile private var sunImage = ByteArray(0)

    init {
        scheduledExecutorService.scheduleAtFixedRate(::refreshImageOfSun, 0L, 15L, TimeUnit.MINUTES)
    }

    val objectTypes: Collection<SkyObjectType> by lazy { skyObjectEntityRepository.objectTypes }

    fun imageOfSun(): ByteArray {
        return sunImage
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
        val target = requireNotNull(cachedSkyObjectEntities[id] ?: skyObjectEntityRepository[id]) { "cannot found sky object: [$id]" }
        cachedSkyObjectEntities[id] = target
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
            if (location is Location) positions.getOrPut(location) { location.geographicPosition() }
            else location.geographicPosition()
        }

        val lock = synchronized(targetLocks) { targetLocks.getOrPut(target) { Any() } }

        return synchronized(lock) {
            val offsetInSeconds = location.offsetInSeconds().toLong()
            if (target is Body) bodyEphemerisProvider.compute(target, position, dateTime, offsetInSeconds, fully)
            else horizonsEphemerisProvider.compute(target, position, dateTime, offsetInSeconds, fully)
        }
    }

    fun searchSatellites(text: String = "", groups: List<SatelliteGroupType> = emptyList(), id: Long = 0L): List<SatelliteEntity> {
        return satelliteRepository.search(text.ifBlank { null }, groups, id)
    }

    fun twilight(location: GeographicCoordinate, dateTime: LocalDateTime, fast: Boolean = false): Twilight {
        val civilDusk = doubleArrayOf(0.0, 0.0)
        val nauticalDusk = doubleArrayOf(0.0, 0.0)
        val astronomicalDusk = doubleArrayOf(0.0, 0.0)
        val night = doubleArrayOf(0.0, 0.0)
        val astronomicalDawn = doubleArrayOf(0.0, 0.0)
        val nauticalDawn = doubleArrayOf(0.0, 0.0)
        val civilDawn = doubleArrayOf(0.0, 0.0)

        val ephemeris = bodyEphemeris(if (fast) VSOP87E.SUN else SUN, location, dateTime, true)
        val range = evenlySpacedNumbers(0.0, (ephemeris.size - 1).toDouble(), ephemeris.size)
        val result = findDiscrete(range, TwilightDiscreteFunction(ephemeris), 1.0)

        civilDusk[0] = result.x[0] / 60.0
        civilDusk[1] = result.x[1] / 60.0
        nauticalDusk[0] = result.x[1] / 60.0
        nauticalDusk[1] = result.x[2] / 60.0
        astronomicalDusk[0] = result.x[2] / 60.0
        astronomicalDusk[1] = result.x[3] / 60.0
        night[0] = result.x[3] / 60.0
        night[1] = result.x[4] / 60.0
        astronomicalDawn[0] = result.x[4] / 60.0
        astronomicalDawn[1] = result.x[5] / 60.0
        nauticalDawn[0] = result.x[5] / 60.0
        nauticalDawn[1] = result.x[6] / 60.0
        civilDawn[0] = result.x[6] / 60.0
        civilDawn[1] = result.x[7] / 60.0

        return Twilight(
            civilDusk, nauticalDusk, astronomicalDusk, night,
            astronomicalDawn, nauticalDawn, civilDawn,
        )
    }

    fun altitudePointsOfSun(location: GeographicCoordinate, dateTime: LocalDateTime, stepSize: Int, fast: Boolean = false): List<DoubleArray> {
        val ephemeris = bodyEphemeris(if (fast) VSOP87E.SUN else SUN, location, dateTime, true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfMoon(location: GeographicCoordinate, dateTime: LocalDateTime, stepSize: Int, fast: Boolean = false): List<DoubleArray> {
        val ephemeris = bodyEphemeris(if (fast) FAST_MOON else MOON, location, dateTime, true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfPlanet(
        location: GeographicCoordinate, code: String, dateTime: LocalDateTime,
        stepSize: Int, fast: Boolean = false,
    ): List<DoubleArray> {
        val target: Any = VSOP87E.entries.takeIf { fast }?.find { "${it.target}" == code } ?: code
        val ephemeris = bodyEphemeris(target, location, dateTime, true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfSkyObject(location: GeographicCoordinate, id: Long, dateTime: LocalDateTime, stepSize: Int): List<DoubleArray> {
        val target = requireNotNull(cachedSkyObjectEntities[id] ?: skyObjectEntityRepository[id]) { "cannot found sky object: [$id]" }
        cachedSkyObjectEntities[id] = target
        val ephemeris = bodyEphemeris(target, location, dateTime, true)
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfSatellite(
        location: GeographicCoordinate,
        satellite: SatelliteEntity,
        dateTime: LocalDateTime,
        stepSize: Int,
    ): List<DoubleArray> {
        val ephemeris = bodyEphemeris("TLE@${satellite.tle}", location, dateTime, true)
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
        type: SkyObjectType? = null, id: Long = 0L,
    ) = skyObjectEntityRepository.search(text, constellation, rightAscension, declination, radius, magnitudeMin, magnitudeMax, type, id)

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

    fun moonPhases(location: GeographicCoordinate, dateTime: LocalDateTime, topocentric: Boolean = false): Map<String, Any?>? {
        val hour = dateTime.withMinute(0).withSecond(0).withNano(0)

        synchronized(moonPhaseInfo) {
            if (hour !in moonPhaseInfo) {
                val request = Request.Builder()
                    .url(MOON_PHASE_URL.format(dateTime.minusMinutes(location.offsetInMinutes().toLong()).format(MOON_PHASE_DATE_TIME_FORMAT)))
                    .build()

                val body = try {
                    httpClient.newCall(request).execute()
                        .body?.byteStream()
                        ?.use { objectMapper.readValue(it, MoonPhaseInfo::class.java) }
                } catch (_: Throwable) {
                    null
                }

                moonPhaseInfo[hour] = (body ?: return null)
            }

            moonPhaseInfo[hour]?.lunation = TimeYMDHMS(dateTime).lunation()
        }

        synchronized(moonPhaseDateTime) {
            val firstDayOfMounth = hour.toLocalDate().withDayOfMonth(1)
            val key = firstDayOfMounth to topocentric

            val phases = if (key in moonPhaseDateTime) {
                moonPhaseDateTime[key]!!
            } else {
                val offsetInMinutes = location.offsetInMinutes().toLong()

                if (topocentric) {
                    moonPhaseFinder.find(firstDayOfMounth, location, offsetInMinutes)
                } else {
                    moonPhaseFinder.find(firstDayOfMounth, offsetInMinutes)
                }.also { moonPhaseDateTime[key] = it }
            }

            return mapOf(
                "current" to moonPhaseInfo[hour],
                "phases" to phases,
            )
        }
    }

    fun earthSeasons(location: GeographicCoordinate, year: Int): List<EarthSeasonDateTime> {
        return synchronized(earthSeasonDateTime) {
            if (year in earthSeasonDateTime) {
                earthSeasonDateTime[year]!!
            } else {
                val offsetInMinutes = location.offsetInMinutes().toLong()
                earthSeasonFinder.find(year, offsetInMinutes).also { earthSeasonDateTime[year] = it }
            }
        }
    }

    companion object {

        private const val SUN_IMAGE_URL = "https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIC.jpg"
        private const val MOON_PHASE_URL = "https://svs.gsfc.nasa.gov/api/dialamoon/%s"

        private const val SUN = "10"
        private const val MOON = "301"

        private val FAST_MOON by lazy { VSOP87E.EARTH + ELPMPP02 }
        private val MOON_PHASE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")

        private fun GeographicCoordinate.geographicPosition() = when (this) {
            is GeographicPosition -> this
            else -> Geoid.IERS2010.lonLat(this)
        }

        private fun GeographicCoordinate.offsetInSeconds() = when (this) {
            is TimeZonedInSeconds -> offsetInSeconds
            else -> 0
        }

        private fun GeographicCoordinate.offsetInMinutes() = when (this) {
            is Location -> offsetInMinutes
            else -> offsetInSeconds() / 60
        }

        private fun List<HorizonsElement>.withLocationAndDateTime(location: GeographicCoordinate, dateTime: LocalDateTime): HorizonsElement? {
            val offsetInMinutes = location.offsetInMinutes().toLong()
            return let { HorizonsElement.of(it, dateTime.minusMinutes(offsetInMinutes)) }
        }

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
