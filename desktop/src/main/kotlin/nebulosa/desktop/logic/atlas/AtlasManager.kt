package nebulosa.desktop.logic.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import javafx.geometry.Point2D
import nebulosa.constants.DAYSEC
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.atlas.ephemeris.provider.BodyEphemerisProvider
import nebulosa.desktop.logic.atlas.ephemeris.provider.HorizonsEphemerisProvider
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.newEventBus
import nebulosa.desktop.logic.observeOnJavaFX
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.atlas.Twilight
import nebulosa.indi.device.mount.Mount
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.query.horizons.HorizonsElement
import nebulosa.query.horizons.HorizonsEphemeris
import nebulosa.query.horizons.HorizonsQuantity
import nebulosa.query.sbd.SmallBody
import nebulosa.query.sbd.SmallBodyDatabaseLookupService
import nebulosa.query.simbad.SimbadObject
import nebulosa.query.simbad.SimbadQuery
import nebulosa.query.simbad.SimbadService
import nebulosa.time.UTC
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
import java.time.ZoneOffset
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.concurrent.timer
import kotlin.math.hypot
import kotlin.math.max

@Component
class AtlasManager(@Autowired private val view: AtlasView) : Closeable {

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var bodyEphemerisProvider: BodyEphemerisProvider
    @Autowired private lateinit var horizonsEphemerisProvider: HorizonsEphemerisProvider
    @Autowired private lateinit var smallBodyDatabaseLookupService: SmallBodyDatabaseLookupService
    @Autowired private lateinit var simbadService: SimbadService
    @Autowired private lateinit var systemExecutorService: ExecutorService

    private val pointsCache = hashMapOf<HorizonsEphemeris, List<Point2D>>()

    private val timerCount = AtomicInteger()
    private val timer = timer(daemon = true, initialDelay = 60000L, period = 60000L) { onTimerHit(timerCount.getAndIncrement()) }
    private val observerEventBus = newEventBus<Double>()

    @Volatile private var observer: GeographicPosition? = null
    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var planet: AtlasView.Planet? = null
    @Volatile private var minorPlanet: SmallBody? = null
    @Volatile private var star: AtlasView.Star? = null
    @Volatile private var dso: AtlasView.DSO? = null
    @Volatile private var bodyName = ""

    val mountProperty
        get() = equipmentManager.selectedMount

    val mount: Mount?
        get() = mountProperty.value

    @PostConstruct
    private fun initialize() {
        val longitude = mount?.longitude ?: preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = mount?.latitude ?: preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = mount?.elevation ?: preferences.double("atlas.elevation")?.au ?: Distance.ZERO

        observer = Geoid.IERS2010.latLon(longitude, latitude, elevation)
        updateTitle()

        observerEventBus
            .debounce(1L, TimeUnit.SECONDS)
            .observeOnJavaFX()
            .subscribe { onMountCoordinateChanged() }

        mountProperty.latitudeProperty.on(observerEventBus::onNext)
        mountProperty.longitudeProperty.on(observerEventBus::onNext)
        mountProperty.elevationProperty.on(observerEventBus::onNext)
    }

    private fun onMountCoordinateChanged() {
        val mount = mount ?: return

        observer = Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)
        updateTitle()

        preferences.double("atlas.longitude", mount.longitude.value)
        preferences.double("atlas.latitude", mount.latitude.value)
        preferences.double("atlas.elevation", mount.elevation.value)

        pointsCache.clear()

        computeTab()
    }

    private fun updateTitle() {
        view.title = "Atlas · LAT: %.04f° LNG: %.04f° ELEV: %.0fm".format(
            observer!!.latitude.degrees, observer!!.longitude.degrees, observer!!.elevation.meters
        )
    }

    fun computeTab(type: AtlasView.TabType = tabType) {
        if (!view.showing) return

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

    fun computeSun() {
        bodyName = "Sun"
        "10".computeBody()
    }

    fun computeMoon() {
        bodyName = "Moon"
        "301".computeBody()
    }

    fun computePlanet(body: AtlasView.Planet? = planet) {
        planet = body ?: return
        bodyName = body.name
        body.command.computeBody()
    }

    fun computeMinorPlanet(body: SmallBody? = minorPlanet) {
        minorPlanet = body ?: return
        bodyName = body.body!!.fullname
        "DES=${body.body!!.spkId};".computeBody()
    }

    fun computeStar(body: AtlasView.Star? = star) {
        star = body ?: return
        bodyName = body.simbad.names.joinToString(", ") { it.type.format(it.name) }
        body.star.computeBody()
    }

    fun computeDSO(body: AtlasView.DSO? = dso) {
        dso = body ?: return
        bodyName = body.simbad.names.joinToString(", ") { it.type.format(it.name) }
        body.star.computeBody()
    }

    private fun String.computeBody() {
        if (isNotEmpty()) computeAltitude(this)
    }

    private fun Body.computeBody() {
        computeAltitude(this)
    }

    private fun HorizonsEphemeris.makePoints(): List<Point2D> {
        if (this in pointsCache) return pointsCache[this]!!

        val startTime = start.toEpochSecond(ZoneOffset.UTC)
        val points = ArrayList<Point2D>(size / 5 + 3)
        var counter = 0

        forEach {
            val x = (it.key.toEpochSecond(ZoneOffset.UTC) - startTime) / DAYSEC
            val y = max(0.0, (it.value[HorizonsQuantity.APPARENT_ALT]!!.toDoubleOrNull() ?: 0.0) / 90.0)
            if (counter++ % 5 == 0) points.add(Point2D(x, y))
        }

        pointsCache[this] = points

        return points
    }

    private fun drawAltitude(points: List<Point2D>) {
        javaFxThread {
            view.updateAltitude(
                points = points,
                now = 0.0,
                civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                astronomicalTwilight = Twilight.EMPTY,
            )
        }
    }

    private fun computeAltitude(target: Any, force: Boolean = false) {
        LOG.info("computing altitude. target={}", target)

        val observer = observer ?: return

        if (target is String) {
            systemExecutorService.submit {
                val ephemeris = horizonsEphemerisProvider.compute(target, observer, force)

                if (ephemeris == null) {
                    LOG.warn("unable to retrieve ephemeris. target={}", target)
                } else if (ephemeris.isNotEmpty()) {
                    LOG.info("ephemeris was retrieved. target={}, start={}, end={}", target, ephemeris.start, ephemeris.endInclusive)

                    ephemeris.showBodyCoordinatesAndInfos(target)
                } else {
                    LOG.warn("retrived empty epheremis. target={}", target)
                }
            }
        } else if (target is Body) {
            systemExecutorService.submit {
                val ephemeris = bodyEphemerisProvider.compute(target, observer, force)
                    ?: return@submit view.clearAltitudeAndCoordinates()
                ephemeris.showBodyCoordinatesAndInfos(target)
            }
        }
    }

    private fun HorizonsEphemeris.showBodyCoordinatesAndInfos(target: Any) {
        computeCoordinates(target)
        javaFxThread { view.updateInfo(bodyName) }
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
        javaFxThread { view.updateEquatorialCoordinates(ra, dec, raJ2000, decJ2000, constellation) }
    }

    private fun computeHorizontalCoordinates(element: HorizonsElement) {
        val az = element[HorizonsQuantity.APPARENT_AZ]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val alt = element[HorizonsQuantity.APPARENT_ALT]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        javaFxThread { view.updateHorizontalCoordinates(az, alt) }
    }

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

            javaFxThread { view.updateSunImage(sunImagePath.toUri().toString()) }
        }
    }

    fun updateMoonImage() {
        if (!view.showing) return

        javaFxThread { view.updateMoonImage("images/MOON.png") }
    }

    fun searchMinorPlanet(text: String) {
        smallBodyDatabaseLookupService.search(text)
            .enqueue(object : Callback<SmallBody> {

                override fun onResponse(call: Call<SmallBody>, response: Response<SmallBody>) {
                    val smallBody = response.body() ?: return

                    javaFxThread {
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
            val dso = simbadService.query(query).execute().body()!!

            javaFxThread { view.populateDSO(dso.map { AtlasView.DSO(it) }) }
        }
    }

    fun goTo(ra: Angle, dec: Angle) {
        LOG.info("go to. ra={}, dec={}", ra.hours, dec.degrees)
        mount?.goTo(ra, dec)
    }

    fun slewTo(ra: Angle, dec: Angle) {
        LOG.info("slew to. ra={}, dec={}", ra.hours, dec.degrees)
        mount?.slewTo(ra, dec)
    }

    fun sync(ra: Angle, dec: Angle) {
        LOG.info("sync. ra={}, dec={}", ra.hours, dec.degrees)
        mount?.sync(ra, dec)
    }

    private fun onTimerHit(count: Int) {
        if (!view.showing) return

        if (count % 15 == 0) updateSunImage()

        javaFxThread { computeTab() }
    }

    fun savePreferences() {
        preferences.double("atlas.screen.x", view.x)
        preferences.double("atlas.screen.y", view.y)
    }

    fun loadPreferences() {
        preferences.double("atlas.screen.x")?.also { view.x = it }
        preferences.double("atlas.screen.y")?.also { view.y = it }
    }

    override fun close() {
        savePreferences()

        timer.cancel()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AtlasManager::class.java)
    }
}
