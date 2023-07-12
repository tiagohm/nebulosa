package nebulosa.api.services

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletResponse
import nebulosa.api.data.entities.AppPreferenceEntity
import nebulosa.api.data.entities.DeepSkyObjectEntity
import nebulosa.api.data.entities.LocationEntity
import nebulosa.api.data.entities.StarEntity
import nebulosa.api.data.responses.BodyPositionResponse
import nebulosa.api.data.responses.MinorPlanetResponse
import nebulosa.api.data.responses.TwilightResponse
import nebulosa.api.repositories.AppPreferenceRepository
import nebulosa.api.repositories.DeepSkyObjectRepository
import nebulosa.api.repositories.StarRepository
import nebulosa.api.services.algorithms.TwilightDiscreteFunction
import nebulosa.api.services.ephemeris.BodyEphemerisProvider
import nebulosa.api.services.ephemeris.HorizonsEphemerisProvider
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.io.resource
import nebulosa.io.transferAndCloseInput
import nebulosa.log.loggerFor
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.almanac.evenlySpacedNumbers
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.GeographicPosition
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.skycatalog.SkyObject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
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
    private val smallBodyDatabaseLookupService: SmallBodyDatabaseLookupService,
    private val starRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val appPreferenceRepository: AppPreferenceRepository,
    private val okHttpClient: OkHttpClient,
) {

    @Value("classpath:data/dsos.json.gz")
    private lateinit var dsoResource: Resource

    @Value("classpath:data/stars.json.gz")
    private lateinit var starResource: Resource

    private val positions = HashMap<LocationEntity, GeographicPosition>()
    private val stars = HashMap<Long, FixedStar>()
    private val dsos = HashMap<Long, FixedStar>()
    @Volatile private var sunImage = ByteArray(0)

    @PostConstruct
    private fun initialize() {
        if (appPreferenceRepository.withKey("database.version")?.value != DATABASE_VERSION) {
            starRepository.load(starResource)
            deepSkyObjectRepository.load(dsoResource)

            appPreferenceRepository
                .save(AppPreferenceEntity(key = "database.version", value = DATABASE_VERSION))
        }

        LOG.info("DSO/Star database version $DATABASE_VERSION")
    }

    fun imageOfSun(output: HttpServletResponse) {
        output.contentType = "image/png"
        output.outputStream.write(sunImage)
    }

    fun imageOfMoon(
        location: LocationEntity, dateTime: LocalDateTime,
        output: HttpServletResponse,
    ) {
        val sot = bodyEphemeris(MOON, location, dateTime)
            .withLocationAndDateTime(location, dateTime)!!
            .get(HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE)
            ?.split(",") ?: return

        val angle = sot[0].toDouble()
        val leading = sot[1] == "/L"
        val phase = if (leading) 360.0 - angle else angle
        val age = 29.53058868 * (phase / 360.0)

        LOG.info("moon phase. phase={}, age={}, location={}, dateTime={}", phase, age, location, dateTime)

        val phaseNum = ((age * 1.01589576574604) % 30.0).toInt() + 1

        output.contentType = "image/png"
        resource("images/moonPhases/%02d.png".format(phaseNum))!!
            .transferAndCloseInput(output.outputStream)
    }

    fun positionOfSun(location: LocationEntity, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(SUN, location, dateTime)!!
    }

    fun positionOfMoon(location: LocationEntity, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(MOON, location, dateTime)!!
    }

    fun positionOfPlanet(location: LocationEntity, code: String, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(code, location, dateTime)!!
    }

    fun positionOfStar(location: LocationEntity, star: StarEntity, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(fixedStarOf(star), location, dateTime)!!
            .copy(magnitude = star.magnitude, constellation = star.constellation, distance = star.distance, distanceUnit = "ly")
    }

    fun positionOfDSO(location: LocationEntity, dso: DeepSkyObjectEntity, dateTime: LocalDateTime): BodyPositionResponse {
        return positionOfBody(fixedStarOf(dso), location, dateTime)!!
            .copy(magnitude = dso.magnitude, constellation = dso.constellation, distance = dso.distance, distanceUnit = "ly")
    }

    private fun positionOfBody(target: Any, location: LocationEntity, dateTime: LocalDateTime): BodyPositionResponse? {
        return bodyEphemeris(target, location, dateTime)
            .withLocationAndDateTime(location, dateTime)
            ?.let(BodyPositionResponse::of)
    }

    private fun bodyEphemeris(target: Any, location: LocationEntity, dateTime: LocalDateTime): List<HorizonsElement> {
        val position = positions.getOrPut(location, location::geographicPosition)
        val offsetInSeconds = location.offsetInMinutes * 60
        val zoneId = ZoneOffset.ofTotalSeconds(offsetInSeconds)
        return if (target is Body) bodyEphemerisProvider.compute(target, position, dateTime, zoneId)
        else horizonsEphemerisProvider.compute(target, position, dateTime, zoneId)
    }

    fun twilight(location: LocationEntity, date: LocalDate): TwilightResponse {
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
        val ephemeris = bodyEphemeris(fixedStarOf(star), location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    fun altitudePointsOfDSO(location: LocationEntity, dso: DeepSkyObjectEntity, date: LocalDate, stepSize: Int): List<DoubleArray> {
        val ephemeris = bodyEphemeris(fixedStarOf(dso), location, LocalDateTime.of(date, LocalTime.now()))
        return altitudePointsOfBody(ephemeris, stepSize)
    }

    private fun fixedStarOf(star: StarEntity): FixedStar {
        return stars.getOrPut(star.id) {
            FixedStar(
                star.rightAscension.rad, star.declination.rad,
                star.pmRA.rad, star.pmDEC.rad, star.parallax.mas, star.radialVelocity.kms
            )
        }
    }

    private fun fixedStarOf(dso: DeepSkyObjectEntity): FixedStar {
        return dsos.getOrPut(dso.id) {
            FixedStar(
                dso.rightAscension.rad, dso.declination.rad,
                dso.pmRA.rad, dso.pmDEC.rad, dso.parallax.mas, dso.radialVelocity.kms
            )
        }
    }

    private fun altitudePointsOfBody(ephemeris: List<HorizonsElement>, stepSize: Int): List<DoubleArray> {
        val points = ArrayList<DoubleArray>(1441)

        evenlySpacedNumbers(0.0, 1440.0, 1441 / stepSize) {
            val minute = it.toInt()
            val altitude = ephemeris[minute].asDouble(HorizonsQuantity.APPARENT_ALT)
            points.add(doubleArrayOf(minute / 60.0, altitude))
        }

        return points
    }

    fun searchMinorPlanet(text: String): MinorPlanetResponse {
        return smallBodyDatabaseLookupService
            .search(text).execute().body()
            ?.let(MinorPlanetResponse::of)
            ?: MinorPlanetResponse.EMPTY
    }

    fun searchStar(text: String): List<StarEntity> {
        return starRepository.search(text)
    }

    fun searchDSO(text: String): List<DeepSkyObjectEntity> {
        return deepSkyObjectRepository.search(text)
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

        const val DATABASE_VERSION = "2023.07.09"

        private const val SUN_IMAGE_URL = "https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIC.jpg"

        private const val SUN = "10"
        private const val MOON = "301"

        @JvmStatic private val LOG = loggerFor<AtlasService>()

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
