package nebulosa.desktop.logic.atlas

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
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.*
import nebulosa.nova.position.Astrometric
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.query.horizons.HorizonsService
import nebulosa.query.horizons.SpkFile
import nebulosa.query.sbd.SmallBody
import nebulosa.query.sbd.SmallBodyDatabaseLookupService
import nebulosa.time.TimeJD
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import okio.ByteString.Companion.decodeBase64
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.awt.image.BufferedImage
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.math.hypot
import kotlin.math.max

class AtlasManager(private val view: AtlasView) : KoinComponent, Closeable {

    private val equipmentManager by inject<EquipmentManager>()
    private val preferences by inject<Preferences>()
    private val appDirectory by inject<Path>(named("app"))

    private val bodyCache = HashMap<Body, List<Point2D>>()
    private val minorPlanetCache = HashMap<Int, Body>()

    private val timerCount = AtomicInteger()
    private val timer = timer(daemon = true, initialDelay = 30000L, period = 30000L) { onTimerHit(timerCount.getAndIncrement()) }

    private val smallBodyDatabaseLookupService = SmallBodyDatabaseLookupService()
    private val horizonsService = HorizonsService()

    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var observer: Body
    @Volatile private var planet: Body? = null
    @Volatile private var position: ICRF? = null
    @Volatile private var minorPlanet: SmallBody? = null

    val mountProperty = equipmentManager.selectedMount

    val mount: Mount? get() = mountProperty.value

    init {
        val longitude = mount?.longitude ?: preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = mount?.latitude ?: preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = mount?.elevation ?: preferences.double("atlas.elevation")?.au ?: Distance.ZERO

        observer = VSOP87E.EARTH + Geoid.IERS2010.latLon(longitude, latitude, elevation)

        mountProperty.on { onMountCoordinateChanged() }

        EventBus.DEVICE
            .subscribe(
                filter = { it is MountGeographicCoordinateChanged && it.device === mount },
                observeOnJavaFX = true,
            ) { onMountCoordinateChanged() }
    }

    private fun onMountCoordinateChanged() {
        val mount = mount ?: return

        observer = VSOP87E.EARTH + Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)

        preferences.double("atlas.longitude", mount.longitude.value)
        preferences.double("atlas.latitude", mount.latitude.value)
        preferences.double("atlas.elevation", mount.elevation.value)

        bodyCache.clear()

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
            AtlasView.Planet("Mercury", "Planet", VSOP87E.MERCURY),
            AtlasView.Planet("Venus", "Planet", VSOP87E.VENUS),
            AtlasView.Planet("Mars", "Planet", VSOP87E.MARS),
            AtlasView.Planet("Jupiter", "Planet", VSOP87E.JUPITER),
            AtlasView.Planet("Saturn", "Planet", VSOP87E.SATURN),
            AtlasView.Planet("Uranus", "Planet", VSOP87E.URANUS),
            AtlasView.Planet("Neptune", "Planet", VSOP87E.NEPTUNE),
            AtlasView.Planet("Pluto", "Dwarf Planet", VSOP87E.MERCURY),
            AtlasView.Planet("Ariel", "Uranus' Satellite", VSOP87E.URANUS + GUST86.ARIEL),
            AtlasView.Planet("Umbriel", "Uranus' Satellite", VSOP87E.URANUS + GUST86.UMBRIEL),
            AtlasView.Planet("Titania", "Uranus' Satellite", VSOP87E.URANUS + GUST86.TITANIA),
            AtlasView.Planet("Oberon", "Uranus' Satellite", VSOP87E.URANUS + GUST86.OBERON),
            AtlasView.Planet("Miranda", "Uranus' Satellite", VSOP87E.URANUS + GUST86.MIRANDA),
        )

        view.populatePlanets(planets)
    }

    fun computeSun() {
        VSOP87E.SUN.computeBody()
    }

    fun computeMoon() {
        MOON.computeBody()
    }

    fun computePlanet(body: Body? = planet) {
        body?.computeBody() ?: return view.clearAltitudeAndCoordinates()
        planet = body
    }

    fun computeMinorPlanet(body: SmallBody? = minorPlanet) {
        minorPlanet = body ?: return view.clearAltitudeAndCoordinates()

        val spkId = body.body!!.spkId

        if (spkId in minorPlanetCache) {
            return minorPlanetCache[spkId]!!.computeBody()
        }

        thread {
            val startTime = LocalDateTime.now().minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0)
            val endTime = LocalDateTime.now().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59)
            val spkFilename = "%04d%02d-%04d%02d-%s.spk".format(startTime.year, startTime.monthValue, endTime.year, endTime.monthValue, spkId)
            val spkPath = Paths.get("$appDirectory", "spks", spkFilename)

            // TODO: Clean up old SPK paths.

            fun computeBodyFromPath() {
                val kernel = SpiceKernel(Spk(SourceDaf(spkPath.toFile())))
                val minorPlanetBody = VSOP87E.SUN + kernel[spkId]
                minorPlanetCache[spkId] = minorPlanetBody

                Platform.runLater { minorPlanetBody.computeBody() }
            }

            if (spkPath.exists()) {
                computeBodyFromPath()
            } else {
                horizonsService
                    .spk(spkId, startTime, endTime)
                    .enqueue(object : Callback<SpkFile> {

                        override fun onResponse(call: Call<SpkFile>, response: Response<SpkFile>) {
                            val spkFile = response.body() ?: return

                            if (spkFile.error.isNotEmpty()) {
                                Platform.runLater { view.showAlert(spkFile.error) }
                            } else {
                                val spkDecoded = spkFile.spk.decodeBase64() ?: return
                                spkPath.parent.createDirectories()
                                spkPath.outputStream().use(spkDecoded::write)
                                computeBodyFromPath()
                            }
                        }

                        override fun onFailure(call: Call<SpkFile>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
            }
        }
    }

    private fun Body.computeBody() {
        computeAltitude(observer, this)
        computeCoordinates(observer, this)
    }

    private fun computeAltitude(observer: Body, target: Body) {
        val now = OffsetDateTime.now()

        val year = now.year
        val month = now.monthValue
        val dayOfMonth = now.dayOfMonth

        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = TimeYMDHMS(year, month, dayOfMonth, 12) - offset
        val stepCount = 24.0 * 2.0

        if (target in bodyCache) {
            view.drawAltitude(
                points = bodyCache[target]!!,
                now = 0.0,
                civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                astronomicalTwilight = Twilight.EMPTY,
            )
        } else {
            thread {
                val points = ArrayList<Point2D>(stepCount.toInt())

                for (i in 0..stepCount.toInt()) {
                    val fraction = i / stepCount // 0..1
                    val position = observer.at<Barycentric>(UTC(startTime + fraction)).observe(target)
                    val altitude = position.horizontal().latitude
                    val y = max(0.0, altitude.degrees / 90.0)
                    points.add(Point2D(fraction, y))
                }

                bodyCache[target] = points

                Platform.runLater {
                    view.drawAltitude(
                        points = points,
                        now = 0.0,
                        civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                        astronomicalTwilight = Twilight.EMPTY,
                    )
                }
            }
        }
    }

    private fun computeCoordinates(observer: Body, target: Body) {
        val position = observer.at<Barycentric>(UTC.now()).observe(target)
        computeEquatorialCoordinates(position)
        computeHorizontalCoordinates(position)
        this.position = position
    }

    private fun computeEquatorialCoordinates(position: Astrometric) {
        val (ra, dec) = position.equatorialAtDate()
        val (raJ2000, decJ2000) = position.equatorialJ2000()
        view.updateEquatorialCoordinates(ra, dec, raJ2000, decJ2000)
    }

    private fun computeHorizontalCoordinates(position: Astrometric) {
        val (az, alt) = position.horizontal()
        view.updateHorizontalCoordinates(az, alt)
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

                    if (distance > 224) {
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
                            val minorPlanets = smallBody.orbit!!
                                .elements.map {
                                    AtlasView.MinorPlanet(
                                        it.label, it.title,
                                        if (it.value != null) "${it.value ?: ""} ${it.units ?: ""}" else "",
                                    )
                                }

                            view.populateMinorPlanet(minorPlanets)
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
        val (ra, dec) = position?.equatorialAtDate() ?: return
        mount?.goTo(ra, dec)
    }

    fun slewTo() {
        val (ra, dec) = position?.equatorialAtDate() ?: return
        mount?.slewTo(ra, dec)
    }

    fun sync() {
        val (ra, dec) = position?.equatorialAtDate() ?: return
        mount?.sync(ra, dec)
    }

    private fun onTimerHit(count: Int) {
        if (!view.showing) return

        // 15 min.
        if (count % 30 == 0) updateSunImage()

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

        @JvmStatic private val MOON = VSOP87E.EARTH + ELPMPP02
    }
}
