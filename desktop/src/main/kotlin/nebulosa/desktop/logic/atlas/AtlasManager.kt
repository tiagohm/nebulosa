package nebulosa.desktop.logic.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.atlas.ephemeris.provider.BodyEphemerisProvider
import nebulosa.desktop.logic.atlas.ephemeris.provider.HorizonsEphemerisProvider
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.framing.FramingView
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.pmod
import nebulosa.nova.almanac.DiscreteFunction
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.sbd.SmallBody
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadObject
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService
import nebulosa.time.UTC
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.awt.image.BufferedImage
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import kotlin.math.hypot

@Component
@EnableScheduling
class AtlasManager(@Autowired internal val view: AtlasView) : Closeable {

    private val ephemerisCache = hashMapOf<Any, HorizonsEphemeris?>()
    private val pointsCache = hashMapOf<HorizonsEphemeris, List<XYItem>>()

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var bodyEphemerisProvider: BodyEphemerisProvider
    @Autowired private lateinit var horizonsEphemerisProvider: HorizonsEphemerisProvider
    @Autowired private lateinit var smallBodyDatabaseLookupService: SmallBodyDatabaseLookupService
    @Autowired private lateinit var simbadService: SimbadService
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Lazy @Autowired private lateinit var framingView: FramingView

    @Volatile private var observer: GeographicPosition? = null
    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var planet: AtlasView.Planet? = null
    @Volatile private var minorPlanet: SmallBody? = null
    @Volatile private var star: AtlasView.Star? = null
    @Volatile private var dso: AtlasView.DSO? = null
    @Volatile private var bodyName = ""

    private val civilDusk = doubleArrayOf(0.0, 0.0)
    private val nauticalDusk = doubleArrayOf(0.0, 0.0)
    private val astronomicalDusk = doubleArrayOf(0.0, 0.0)
    private val night = doubleArrayOf(0.0, 0.0)
    private val astronomicalDawn = doubleArrayOf(0.0, 0.0)
    private val nauticalDawn = doubleArrayOf(0.0, 0.0)
    private val civilDawn = doubleArrayOf(0.0, 0.0)

    val mountProperty
        get() = equipmentManager.selectedMount

    val mount: Mount?
        get() = mountProperty.value

    val computing = SimpleBooleanProperty()

    fun initialize() {
        eventBus.register(this)

        val longitude = mount?.longitude ?: preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = mount?.latitude ?: preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = mount?.elevation ?: preferences.double("atlas.elevation")?.au ?: Distance.ZERO

        observer = Geoid.IERS2010.latLon(longitude, latitude, elevation)
        updateTitle()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        if (event.device !== equipmentManager.selectedMount.value) return

        when (event) {
            is MountGeographicCoordinateChanged -> {
                observer = Geoid.IERS2010.latLon(event.device.longitude, event.device.latitude, event.device.elevation)
                updateTitle()

                preferences.double("atlas.longitude", event.device.longitude.value)
                preferences.double("atlas.latitude", event.device.latitude.value)
                preferences.double("atlas.elevation", event.device.elevation.value)

                ephemerisCache.clear()
                pointsCache.clear()

                computeSun()
                    ?.whenComplete { _, _ -> computeTab() }
                    ?: computeTab()
            }
        }
    }

    private fun updateTitle() {
        view.title = "Atlas · LAT: %.04f° LNG: %.04f° ELEV: %.0fm".format(
            observer!!.latitude.degrees, observer!!.longitude.degrees, observer!!.elevation.meters
        )
    }

    fun computeTab(type: AtlasView.TabType) {
        if (!view.showing) return

        LOG.info("computing tab. type={}", type)

        tabType = type

        when (type) {
            AtlasView.TabType.SUN -> computeSun()
            AtlasView.TabType.MOON -> computeMoon()
            AtlasView.TabType.PLANET -> computePlanet()
            AtlasView.TabType.MINOR_PLANET -> computeMinorPlanet()
            AtlasView.TabType.STAR -> computeStar()
            AtlasView.TabType.DSO -> computeDSO()
        }
    }

    @Scheduled(cron = "0 * * * * *")
    fun computeTab() {
        computeTab(tabType)
    }

    fun populatePlanets() {
        val planets = listOf(
            AtlasView.Planet("Mercury", "Planet", "199"),
            AtlasView.Planet("Venus", "Planet", "299"),
            AtlasView.Planet("Mars", "Planet", "499"),
            AtlasView.Planet("Jupiter", "Planet", "599"),
            AtlasView.Planet("Saturn", "Planet", "699"),
            AtlasView.Planet("Uranus", "Planet", "799"),
            AtlasView.Planet("Neptune", "Planet", "899"),
            AtlasView.Planet("Pluto", "Dwarf Planet", "999"),
            AtlasView.Planet("Phobos", "Mars' Satellite", "401"),
            AtlasView.Planet("Deimos", "Mars' Satellite", "402"),
            AtlasView.Planet("Io", "Jupiter's Satellite", "501"),
            AtlasView.Planet("Europa", "Jupiter's Satellite", "402"),
            AtlasView.Planet("Ganymede", "Jupiter's Satellite", "403"),
            AtlasView.Planet("Callisto", "Jupiter's Satellite", "504"),
            AtlasView.Planet("Mimas", "Saturn's Satellite", "601"),
            AtlasView.Planet("Enceladus", "Saturn's Satellite", "602"),
            AtlasView.Planet("Tethys", "Saturn's Satellite", "603"),
            AtlasView.Planet("Dione", "Saturn's Satellite", "604"),
            AtlasView.Planet("Rhea", "Saturn's Satellite", "605"),
            AtlasView.Planet("Titan", "Saturn's Satellite", "606"),
            AtlasView.Planet("Hyperion", "Saturn's Satellite", "607"),
            AtlasView.Planet("Iapetus", "Saturn's Satellite", "608"),
            AtlasView.Planet("Ariel", "Uranus' Satellite", "701"),
            AtlasView.Planet("Umbriel", "Uranus' Satellite", "702"),
            AtlasView.Planet("Titania", "Uranus' Satellite", "703"),
            AtlasView.Planet("Oberon", "Uranus' Satellite", "704"),
            AtlasView.Planet("Miranda", "Uranus' Satellite", "705"),
            AtlasView.Planet("Triton", "Neptune's Satellite", "801"),
            AtlasView.Planet("Charon", "Pluto's Satellite", "901"),
            AtlasView.Planet("1 Ceres", "Dwarf Planet", "1;"),
            AtlasView.Planet("90377 Sedna", "Dwarf Planet", "90377;"),
            AtlasView.Planet("136199 Eris", "Dwarf Planet", "136199;"),
            AtlasView.Planet("2 Pallas", "Asteroid", "2;"),
            AtlasView.Planet("3 Juno", "Asteroid", "3;"),
            AtlasView.Planet("4 Vesta", "Asteroid", "4;"),
        )

        view.populatePlanet(planets)
    }

    fun populateStars() {
        val stars = objectMapper.readValue(resource("data/NAMED_STARS.json"), Array<SimbadObject>::class.java)
        view.populateStar(stars.map { AtlasView.Star(it) })
    }

    fun computeSun(): CompletableFuture<HorizonsEphemeris>? {
        bodyName = "Sun"
        return "10".computeBody()
    }

    fun computeMoon(): CompletableFuture<HorizonsEphemeris>? {
        bodyName = "Moon"
        return "301".computeBody()
    }

    fun computePlanet(body: AtlasView.Planet? = planet): CompletableFuture<HorizonsEphemeris>? {
        planet = body ?: return null
        bodyName = body.name
        return body.command.computeBody()
    }

    fun computeMinorPlanet(body: SmallBody? = minorPlanet): CompletableFuture<HorizonsEphemeris>? {
        minorPlanet = body ?: return null
        bodyName = body.body!!.fullname
        return "DES=${body.body!!.spkId};".computeBody()
    }

    fun computeStar(body: AtlasView.Star? = star): CompletableFuture<HorizonsEphemeris>? {
        star = body ?: return null
        bodyName = body.simbad.names.joinToString(", ") { it.type.format(it.name) }
        return body.star.computeBody()
    }

    fun computeDSO(body: AtlasView.DSO? = dso): CompletableFuture<HorizonsEphemeris>? {
        dso = body ?: return null
        bodyName = body.simbad.names.joinToString(", ") { it.type.format(it.name) }
        return body.star.computeBody()
    }

    private fun String.computeBody(): CompletableFuture<HorizonsEphemeris>? {
        return if (isNotEmpty()) computeAltitude(this) else null
    }

    private fun Body.computeBody(): CompletableFuture<HorizonsEphemeris>? {
        return computeAltitude(this)
    }

    private fun HorizonsEphemeris.makePoints(): List<XYItem> {
        if (this in pointsCache) return pointsCache[this]!!

        val points = ArrayList<XYItem>(25 * 2)
        var x = 0.0

        times.forEachIndexed { i, time ->
            if (time.minute % 30 != 0) return@forEachIndexed
            val y = elements[i][HorizonsQuantity.APPARENT_ALT]!!.toDoubleOrNull() ?: 0.0
            points.add(XYChartItem(x, y))
            x += 0.5
        }

        pointsCache[this] = points

        return points
    }

    private fun computeTwilight() {
        val ephemeris = ephemerisCache["10"] ?: return

        LOG.info("computing twilight")

        val altitudes = DoubleArray(ephemeris.elements.size) { ephemeris.elements[it][HorizonsQuantity.APPARENT_ALT]!!.toDouble() }
        val (a) = findDiscrete(0.0, 1440.0, TwilightDiscreteFunction(altitudes), 1.0)

        // Expected discrete values: [4, 3, 2, 1, 2, 3, 4, 0]
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
    }

    private fun drawAltitude(points: List<XYItem>) {
        val now = (LocalTime.now().toSecondOfDay() / 3600.0 - 12.0) pmod 24.0

        LOG.info("drawing altitude chart. now={}", now)

        javaFXExecutorService.execute {
            view.drawAltitude(
                points, now,
                civilDawn, nauticalDawn, astronomicalDawn,
                civilDusk, nauticalDusk, astronomicalDusk,
                night,
            )
        }
    }

    @Synchronized
    private fun computeAltitude(target: Any, force: Boolean = false): CompletableFuture<HorizonsEphemeris>? {
        LOG.info("computing altitude. target={}", target)

        val observer = observer ?: return null

        computing.set(true)

        val task = CompletableFuture<HorizonsEphemeris>()

        systemExecutorService.submit {
            try {
                val ephemeris = when (target) {
                    is String -> horizonsEphemerisProvider.compute(target, observer, force)
                    is Body -> bodyEphemerisProvider.compute(target, observer, force)
                    else -> null
                }

                ephemerisCache[target] = ephemeris

                // Sun.
                if (target == "10") computeTwilight()

                if (ephemeris == null) {
                    view.clearAltitudeAndCoordinates()
                    LOG.error("unable to retrieve ephemeris. target={}", target)
                } else if (ephemeris.isEmpty()) {
                    view.clearAltitudeAndCoordinates()
                    LOG.warn("retrieved empty epheremis. target={}", target)
                } else {
                    LOG.info("ephemeris was retrieved. target={}, start={}, end={}", target, ephemeris.start, ephemeris.endInclusive)
                    ephemeris.showBodyCoordinatesAndInfos(target)
                }

                task.complete(ephemeris)
            } catch (e: Throwable) {
                task.completeExceptionally(e)
                LOG.error("failed to retrieve ephemeris.", e)
            } finally {
                computing.set(false)
            }
        }

        return task
    }

    private fun HorizonsEphemeris.showBodyCoordinatesAndInfos(target: Any) {
        computeCoordinates(target)
        javaFXExecutorService.execute { view.updateInfo(bodyName) }
        drawAltitude(makePoints())
    }

    private fun HorizonsEphemeris.computeCoordinates(target: Any) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = this[now] ?: return
        LOG.info("computing coordinates. now={}, target={}, element={}", now, target, element)
        computeEquatorialCoordinates(element)
        computeHorizontalCoordinates(element)
    }

    private fun computeEquatorialCoordinates(element: HorizonsElement) {
        val raJ2000 = element[HorizonsQuantity.ASTROMETRIC_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val decJ2000 = element[HorizonsQuantity.ASTROMETRIC_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val epoch = UTC.now()
        val constellation = Constellation.find(ICRF.equatorial(ra, dec, time = epoch, epoch = epoch))
        javaFXExecutorService.execute { view.updateEquatorialCoordinates(ra, dec, raJ2000, decJ2000, constellation) }
    }

    private fun computeHorizontalCoordinates(element: HorizonsElement) {
        val az = element[HorizonsQuantity.APPARENT_AZ]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val alt = element[HorizonsQuantity.APPARENT_ALT]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        javaFXExecutorService.execute { view.updateHorizontalCoordinates(az, alt) }
    }

    @Scheduled(cron = "0 */15 * * * *")
    fun updateSunImage() {
        if (!view.showing) return

        systemExecutorService.submit {
            val image = ImageIO.read(URL("https://sdo.gsfc.nasa.gov/assets/img/latest/latest_512_HMIIF.jpg"))
            val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val distance = hypot(x - 256.0, y - 256.0)
                    val color = image.getRGB(x, y)

                    if (distance > 238) {
                        val grayLevel = ((color shr 16 and 0xff) + (color shr 8 and 0xff) + (color and 0xff)) / 3

                        if (grayLevel >= 170) {
                            newImage.setRGB(x, y, color)
                        }
                    } else {
                        newImage.setRGB(x, y, color)
                    }
                }
            }

            val sunImagePath = Paths.get("$appDirectory", "SUN.png")
            ImageIO.write(newImage, "PNG", sunImagePath.toFile())

            LOG.info("saving Sun image. path={}", sunImagePath.toUri())

            javaFXExecutorService.execute { view.updateSunImage(sunImagePath.toUri().toString()) }
        }
    }

    fun updateMoonImage() {
        if (!view.showing) return

        javaFXExecutorService.execute { view.updateMoonImage("images/MOON.png") }
    }

    fun searchMinorPlanet(text: String) {
        smallBodyDatabaseLookupService.search(text)
            .enqueue(object : Callback<SmallBody> {

                override fun onResponse(call: Call<SmallBody>, response: Response<SmallBody>) {
                    val smallBody = response.body() ?: return

                    javaFXExecutorService.execute {
                        if (!smallBody.message.isNullOrEmpty()) {
                            view.showAlert(smallBody.message!!)
                        } else if (!smallBody.list.isNullOrEmpty()) {
                            view.showAlert("Found ${smallBody.list!!.size} record(s). Please refine your search criteria, and try again.")
                        } else {
                            val elements = smallBody
                                .orbit!!
                                .elements.map {
                                    val value = if (it.value != null) "${it.value ?: ""} ${it.units ?: ""}" else ""
                                    AtlasView.MinorPlanet(it.label, it.title, value)
                                }

                            view.populateMinorPlanet(elements)

                            computeMinorPlanet(smallBody)
                        }
                    }
                }

                override fun onFailure(call: Call<SmallBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    fun searchDSO(text: String) {
        val query = SimbadQuery()
            .limit(500)
            .name(text)

        systemExecutorService.submit {
            try {
                val dso = simbadService.query(query).execute().body()!!
                javaFXExecutorService.execute { view.populateDSO(dso.map { AtlasView.DSO(it) }) }
            } catch (e: Throwable) {
                javaFXExecutorService.execute { view.showAlert("Failed to search DSOs: ${e.message}") }
            }
        }
    }

    fun goTo(ra: Angle, dec: Angle) {
        mount?.goTo(ra, dec)
    }

    fun slewTo(ra: Angle, dec: Angle) {
        mount?.slewTo(ra, dec)
    }

    fun sync(ra: Angle, dec: Angle) {
        mount?.sync(ra, dec)
    }

    fun frame(ra: Angle, dec: Angle) {
        framingView.load(ra, dec)
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferences.double("atlas.screen.x", view.x)
        preferences.double("atlas.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("atlas.screen.x")?.also { view.x = it }
        preferences.double("atlas.screen.y")?.also { view.y = it }
    }

    override fun close() {
        savePreferences()

        eventBus.unregister(this)
    }

    private class TwilightDiscreteFunction(private val altitudes: DoubleArray) : DiscreteFunction {

        override val stepSize = 1.0

        override fun compute(x: Double): Int {
            val index = x.toInt()
            val altitude = altitudes[index]

            return when {
                altitude <= ASTRONOMICAL_TWILIGHT -> 1 // Night.
                altitude <= NAUTICAL_TWILIGHT -> 2 // Astronomical.
                altitude <= CIVIL_TWILIGHT -> 3 // Nautical.
                altitude < 0.0 -> 4 // Civil.
                else -> 0
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AtlasManager::class.java)

        private const val ASTRONOMICAL_TWILIGHT = -18.0
        private const val NAUTICAL_TWILIGHT = -12.0
        private const val CIVIL_TWILIGHT = -6.0
    }
}
