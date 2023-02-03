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
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.ELPMPP02
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.Geoid
import nebulosa.time.TimeYMDHMS
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.awt.image.BufferedImage
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.math.hypot
import kotlin.math.max

class AtlasManager(private val view: AtlasView) : KoinComponent, Closeable {

    private val equipmentManager by inject<EquipmentManager>()
    private val preferences by inject<Preferences>()
    private val appDirectory by inject<Path>(named("app"))

    private val cache = HashMap<Body, List<Point2D>>()

    private val timerCount = AtomicInteger()
    private val timer = timer(daemon = true, initialDelay = 60000L, period = 60000L) { onTimerHit() }

    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var observer: Body

    init {
        val longitude = preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = preferences.double("atlas.elevation")?.au ?: Distance.ZERO

        observer = VSOP87E.EARTH + Geoid.IERS2010.latLon(longitude, latitude, elevation)

        equipmentManager.selectedMount.on { onMountCoordinateChanged() }

        EventBus.DEVICE
            .subscribe(
                filter = { it is MountGeographicCoordinateChanged && it.device === equipmentManager.selectedMount.value },
                observeOnJavaFX = true
            ) { onMountCoordinateChanged() }
    }

    private fun onMountCoordinateChanged() {
        val mount = equipmentManager.selectedMount.value ?: return

        observer = VSOP87E.EARTH + Geoid.IERS2010.latLon(mount.longitude, mount.latitude, mount.elevation)

        preferences.double("atlas.longitude", mount.longitude.value)
        preferences.double("atlas.latitude", mount.latitude.value)
        preferences.double("atlas.elevation", mount.elevation.value)

        cache.clear()

        computeTab()
    }

    fun computeTab(type: AtlasView.TabType = tabType) {
        if (!view.showing) return

        tabType = type

        when (type) {
            AtlasView.TabType.SUN -> computeSun()
            AtlasView.TabType.MOON -> computeMoon()
            AtlasView.TabType.PLANET -> Unit
            AtlasView.TabType.MINOR_PLANET -> Unit
            AtlasView.TabType.STAR -> Unit
            AtlasView.TabType.DSO -> Unit
        }
    }

    fun computeSun() {
        VSOP87E.SUN.computeBody()
    }

    fun computeMoon() {
        MOON.computeBody()
    }

    private fun Body.computeBody() {
        computeAltitude(observer, this)
    }

    fun computeAltitude(observer: Body, target: Body) {
        val now = OffsetDateTime.now()

        val year = now.year
        val month = now.monthValue
        val dayOfMonth = now.dayOfMonth

        val offset = now.offset.totalSeconds / DAYSEC
        val startTime = TimeYMDHMS(year, month, dayOfMonth, 12) - offset
        val stepCount = 24.0 * 4.0 // per 15 minutes

        if (target in cache) {
            view.drawAltitudeGraph(
                points = cache[target]!!,
                now = 0.0,
                civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                astronomicalTwilight = Twilight.EMPTY,
            )
        } else {
            thread {
                val points = ArrayList<Point2D>(stepCount.toInt())

                for (i in 0..stepCount.toInt()) {
                    val x = i / stepCount // 0..1
                    val position = observer.at<Barycentric>(startTime + x).observe(target)
                    val altitude = position.horizontal().latitude
                    val y = max(0.0, altitude.degrees / 90.0)
                    points.add(Point2D(x, y))
                }

                cache[target] = points

                Platform.runLater {
                    view.drawAltitudeGraph(
                        points = points,
                        now = 0.0,
                        civilTwilight = Twilight.EMPTY, nauticalTwilight = Twilight.EMPTY,
                        astronomicalTwilight = Twilight.EMPTY,
                    )
                }
            }
        }
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

            val sunImagePath = Paths.get("$appDirectory", "HMIIF.png")

            ImageIO.write(newImage, "PNG", sunImagePath.toFile())

            Platform.runLater { view.updateSunImage("$sunImagePath") }
        }
    }

    private fun onTimerHit() {
        val count = timerCount.getAndIncrement()

        if (count % 15 == 0) updateSunImage()
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
