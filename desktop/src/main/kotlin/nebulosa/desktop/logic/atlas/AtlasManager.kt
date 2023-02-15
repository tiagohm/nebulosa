package nebulosa.desktop.logic.atlas

import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.geometry.Point2D
import nebulosa.constants.DAYSEC
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.on
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.atlas.Twilight
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.query.horizons.HorizonsElement
import nebulosa.query.horizons.HorizonsEphemeris
import nebulosa.query.horizons.HorizonsQuantity
import nebulosa.query.horizons.HorizonsService
import nebulosa.query.sbd.SmallBody
import nebulosa.query.sbd.SmallBodyDatabaseLookupService
import nebulosa.query.simbad.SimbadObject
import nebulosa.time.InstantOfTime
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.awt.image.BufferedImage
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.math.hypot
import kotlin.math.max

class AtlasManager(private val view: AtlasView) : KoinComponent, Closeable {

    private val equipmentManager by inject<EquipmentManager>()
    private val preferences by inject<Preferences>()
    private val objectMapper by inject<ObjectMapper>()
    private val appDirectory by inject<Path>(named("app"))

    private val timespan = ArrayList<Pair<InstantOfTime, LocalDateTime>>(1440)
    private val bodyCache = HashMap<Any, HorizonsEphemeris>()
    private val pointsCache = HashMap<HorizonsEphemeris, List<Point2D>>()

    private val timerCount = AtomicInteger()
    private val timer = timer(daemon = true, initialDelay = 60000L, period = 60000L) { onTimerHit(timerCount.getAndIncrement()) }

    private val smallBodyDatabaseLookupService = SmallBodyDatabaseLookupService()
    private val horizonsService = HorizonsService()

    @Volatile private var observer: GeographicPosition? = null
    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var planet: AtlasView.Planet? = null
    @Volatile private var minorPlanet: SmallBody? = null
    @Volatile private var star: AtlasView.Star? = null

    val mountProperty = equipmentManager.selectedMount

    val mount: Mount?
        get() = mountProperty.value

    init {
        val longitude = mount?.longitude ?: preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = mount?.latitude ?: preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = mount?.elevation ?: preferences.double("atlas.elevation")?.au ?: Distance.ZERO

        observer = Geoid.IERS2010.latLon(longitude, latitude, elevation)

        mountProperty.on { onMountCoordinateChanged() }

        EventBus.DEVICE
            .subscribe(
                filter = { it is MountGeographicCoordinateChanged && it.device === mount },
                observeOnJavaFX = true,
            ) { onMountCoordinateChanged() }
    }

    private fun onMountCoordinateChanged() {
        val mount = mount ?: return

        observer = Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)

        preferences.double("atlas.longitude", mount.longitude.value)
        preferences.double("atlas.latitude", mount.latitude.value)
        preferences.double("atlas.elevation", mount.elevation.value)

        bodyCache.clear()
        pointsCache.clear()

        computeTab()
    }

    fun computeTab(type: AtlasView.TabType = tabType) {
        if (!view.showing) return

        tabType = type

        when (type) {
            AtlasView.TabType.SUN -> computeSun()
            AtlasView.TabType.MOON -> computeMoon()
            AtlasView.TabType.PLANET -> computePlanet()
            AtlasView.TabType.MINOR_PLANET -> computeMinorPlanet()
            AtlasView.TabType.STAR -> Unit
            AtlasView.TabType.DSO -> Unit
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

        view.populatePlanets(planets)
    }

    val currentEphemeris
        get() = when (tabType) {
            AtlasView.TabType.SUN -> bodyCache["10"]
            AtlasView.TabType.MOON -> bodyCache["301"]
            AtlasView.TabType.PLANET -> planet?.command?.let(bodyCache::get)
            AtlasView.TabType.MINOR_PLANET -> minorPlanet?.body?.spkId?.let { bodyCache["DES=${it};"] }
            AtlasView.TabType.STAR -> star?.star?.let(bodyCache::get)
            AtlasView.TabType.DSO -> null
        }

    fun populateStars() {
        val stars = objectMapper.readValue(resource("data/NAMED_STARS.json"), Array<SimbadObject>::class.java)
        view.populateStars(stars.map { AtlasView.Star(it) })
    }

    fun computeSun() {
        "10".computeBody()
    }

    fun computeMoon() {
        "301".computeBody()
    }

    fun computePlanet(body: AtlasView.Planet? = planet) {
        planet = body ?: return
        body.command.computeBody()
    }

    fun computeMinorPlanet(body: SmallBody? = minorPlanet) {
        minorPlanet = body ?: return view.clearAltitudeAndCoordinates()
        "DES=${body.body!!.spkId};".computeBody()
    }

    fun computeStar(body: AtlasView.Star? = star) {
        star = body ?: return view.clearAltitudeAndCoordinates()
        body.star.computeBody()
    }

    private fun String.computeBody() {
        if (isEmpty()) return view.clearAltitudeAndCoordinates()
        computeAltitude(this)
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

    private fun computeTimespan(): Boolean {
        val now = OffsetDateTime.now()

        val year = now.year
        val month = now.monthValue
        val dayOfMonth = now.dayOfMonth

        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = TimeYMDHMS(year, month, dayOfMonth, 12) - offset

        if (timespan.isEmpty() || startTime.value > timespan[0].first.value) {
            LOG.info("computing timespan. startTime={}", startTime)

            timespan.clear()

            val stepCount = 24.0 * 60.0

            for (i in 0L..stepCount.toLong()) {
                val fraction = i / stepCount // 0..1
                timespan.add(UTC(startTime + fraction) to now.plusMinutes(i).toLocalDateTime())
            }

            return true
        }

        return false
    }

    private fun Body.computeEphemeris(force: Boolean = false): HorizonsEphemeris {
        val site = VSOP87E.EARTH + observer!!

        if (force || this !in bodyCache) {
            LOG.info("computing ephemeris. body={}, observer={}", this, observer)

            val ephemeris = HashMap<LocalDateTime, HorizonsElement>(timespan.size)

            timespan.forEach {
                val position = site.at<Barycentric>(it.first).observe(this)
                val (az, alt) = position.horizontal()
                val (ra, dec) = position.equatorialAtDate()
                val (raJ2000, decJ2000) = position.equatorialJ2000()

                val element = HorizonsElement()
                element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.degrees}"
                element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.degrees}"
                element[HorizonsQuantity.APPARENT_RA] = "${ra.degrees}"
                element[HorizonsQuantity.APPARENT_DEC] = "${dec.degrees}"
                element[HorizonsQuantity.APPARENT_AZ] = "${az.degrees}"
                element[HorizonsQuantity.APPARENT_ALT] = "${alt.degrees}"
                ephemeris[it.second] = element
            }

            bodyCache[this] = HorizonsEphemeris.of(ephemeris)
        }

        return bodyCache[this]!!
    }

    private fun drawAltitude(points: List<Point2D>) {
        Platform.runLater {
            view.drawAltitude(
                points = points,
                now = 0.0,
                civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                astronomicalTwilight = Twilight.EMPTY,
            )
        }
    }

    private fun computeAltitude(target: Any, force: Boolean = false) {
        LOG.info("computing altitude. target={}", target)

        if (!force && target in bodyCache) {
            LOG.info("altitude is cached. target={}", target)
            computeCoordinates(target)
            return drawAltitude(bodyCache[target]!!.makePoints())
        }

        val observer = observer ?: return

        val offset = ZoneOffset.systemDefault().rules.getOffset(Instant.now()).totalSeconds.toLong()
        val startTime = LocalDateTime.now().minusHours(12).withHour(12).withMinute(0).minusSeconds(offset)
        val endTime = startTime.plusHours(24)

        if (target is String) {
            thread {
                LOG.info("retrieving ephemeris from JPL Horizons. target={}, startTime={}, endTime={}", target, startTime, endTime)

                val ephemeris = horizonsService
                    .observer(
                        target,
                        observer.longitude, observer.latitude, observer.elevation,
                        startTime, endTime,
                        extraPrecision = true,
                        quantities = DEFAULT_QUANTITIES,
                    ).execute()
                    .body()

                if (ephemeris == null) {
                    LOG.warn("unable to retrieve ephemeris. target={}", target)
                } else if (ephemeris.isNotEmpty()) {
                    LOG.info("ephemeris was retrieved. target={}, start={}, end={}", target, ephemeris.start, ephemeris.endInclusive)

                    bodyCache[target]?.also(pointsCache::remove)
                    bodyCache[target] = ephemeris

                    computeCoordinates(target)

                    drawAltitude(ephemeris.makePoints())
                } else {
                    LOG.warn("retrived empty epheremis. target={}", target)
                }
            }
        } else if (target is Body) {
            thread {
                val ephemeris = target.computeEphemeris(computeTimespan())
                computeCoordinates(target)
                drawAltitude(ephemeris.makePoints())
            }
        }
    }

    private fun computeCoordinates(target: Any) {
        val ephemeris = bodyCache[target] ?: return
        val now = LocalDateTime.now(ZoneOffset.UTC)
        LOG.info("computing coordinates. now={}, target={}, element={}", now, target, ephemeris[now])
        computeEquatorialCoordinates(ephemeris[now] ?: return)
        computeHorizontalCoordinates(ephemeris[now] ?: return)
    }

    private fun computeEquatorialCoordinates(element: HorizonsElement) {
        val raJ2000 = element[HorizonsQuantity.ASTROMETRIC_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val decJ2000 = element[HorizonsQuantity.ASTROMETRIC_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        Platform.runLater { view.updateEquatorialCoordinates(ra, dec, raJ2000, decJ2000) }
    }

    private fun computeHorizontalCoordinates(element: HorizonsElement) {
        val az = element[HorizonsQuantity.APPARENT_AZ]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val alt = element[HorizonsQuantity.APPARENT_ALT]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        Platform.runLater { view.updateHorizontalCoordinates(az, alt) }
    }

    fun updateSunImage() {
        if (!view.showing) return

        thread {
            val image = ImageIO.read(URL("https://sdo.gsfc.nasa.gov/assets/img/latest/latest_512_HMIIF.jpg"))
            val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val distance = hypot(x - 256.0, y - 256.0)
                    val color = image.getRGB(x, y)

                    if (distance > 230) {
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

            Platform.runLater { view.updateSunImage("file://$sunImagePath") }
        }
    }

    fun updateMoonImage() {
        if (!view.showing) return

        Platform.runLater { view.updateMoonImage("images/MOON.png") }
    }

    fun searchAsteroidsAndComets(text: String) {
        smallBodyDatabaseLookupService.search(text)
            .enqueue(object : Callback<SmallBody> {

                override fun onResponse(call: Call<SmallBody>, response: Response<SmallBody>) {
                    val smallBody = response.body() ?: return

                    Platform.runLater {
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

                            view.populateMinorPlanets(elements)
                        }
                    }

                    if (smallBody.body != null) {
                        Platform.runLater { computeMinorPlanet(smallBody) }
                    }
                }

                override fun onFailure(call: Call<SmallBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    fun goTo() {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = currentEphemeris?.get(now) ?: return
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: return
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: return
        mount?.goTo(ra, dec)
    }

    fun slewTo() {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = currentEphemeris?.get(now) ?: return
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: return
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: return
        mount?.slewTo(ra, dec)
    }

    fun sync() {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = currentEphemeris?.get(now) ?: return
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: return
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: return
        mount?.sync(ra, dec)
    }

    private fun onTimerHit(count: Int) {
        if (!view.showing) return

        if (count % 15 == 0) updateSunImage()

        Platform.runLater { computeTab() }
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

        @JvmStatic private val DEFAULT_QUANTITIES = arrayOf(
            HorizonsQuantity.ASTROMETRIC_RA, HorizonsQuantity.ASTROMETRIC_DEC,
            HorizonsQuantity.APPARENT_RA, HorizonsQuantity.APPARENT_DEC,
            HorizonsQuantity.APPARENT_AZ, HorizonsQuantity.APPARENT_ALT,
            HorizonsQuantity.CONSTELLATION
        )
    }
}
