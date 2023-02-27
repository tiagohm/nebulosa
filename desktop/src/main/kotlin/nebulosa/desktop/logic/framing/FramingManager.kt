package nebulosa.desktop.logic.framing

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.disposables.Disposable
import jakarta.annotation.PostConstruct
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.framing.FramingView
import nebulosa.erfa.PairOfAngle
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEquatorialCoordinatesChanged
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewingChanged
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Closeable
import java.io.InterruptedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

@Component
class FramingManager(@Autowired private val view: FramingView) : Closeable {

    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var deviceEventBus: DeviceEventBus
    @Autowired private lateinit var hips2FitsService: Hips2FitsService
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var imageWindowOpener: ImageWindow.Opener
    @Autowired private lateinit var preferences: Preferences

    private val imageWindow = AtomicReference<ImageWindow>()
    private val subscribers = arrayOfNulls<Disposable>(1)
    private val imagePath = AtomicReference<Path>()
    private val framingReloader = FramingReloader()

    val loading = SimpleBooleanProperty()

    val mount
        get() = equipmentManager.selectedMount

    @PostConstruct
    private fun initialize() {
        subscribers[0] = deviceEventBus
            .filter { view.showing && it is MountEvent && it.device === mount.value }
            .cast(MountEvent::class.java)
            .subscribe(::onMountEvent)

        framingReloader.start()
    }

    private fun onMountEvent(event: MountEvent) {
        when (event) {
            is MountEquatorialCoordinatesChanged -> loadFromMountCoordinate(event.device)
            is MountSlewingChanged -> if (!event.device.slewing) loadFromMountCoordinate(event.device)
        }
    }

    fun loadFromMountCoordinate(device: Mount? = null) {
        if (!view.syncFromMount) return

        val mount = device ?: equipmentManager.selectedMount.value ?: return

        mount.computeCoordinates(true, false)

        val coordinate = PairOfAngle(mount.rightAscensionJ2000, mount.declinationJ2000)
        framingReloader.coordinate.set(coordinate)

        javaFxThread { view.updateCoordinate(coordinate.first, coordinate.second) }
    }

    @Synchronized
    fun load(ra: Angle, dec: Angle): CompletableFuture<Path>? {
        if (loading.get()) {
            LOG.warn("framing is already loading")
            return null
        }

        if (!view.showing) {
            LOG.warn("framing window is hidden")
            return null
        }

        loading.set(true)

        val task = CompletableFuture<Path>()

        systemExecutorService.submit {
            val data = try {
                while (view.hipsSurvey == null) Thread.sleep(100L)

                LOG.info(
                    "loading image. survey={}, ra={}, dec={}, width={}, height={}, rotation={}",
                    view.hipsSurvey, ra.hours, dec.degrees,
                    view.frameWidth, view.frameHeight, view.frameRotation.degrees,
                )

                hips2FitsService.query(
                    view.hipsSurvey!!,
                    view.frameRA, view.frameDEC,
                    view.frameWidth, view.frameHeight, view.frameRotation,
                    view.frameFOV,
                    format = FormatOutputType.JPG,
                ).execute().body()!!
            } catch (e: InterruptedIOException) {
                javaFxThread { view.showAlert("Image took a long time to load. Please try again.") }
                task.completeExceptionally(e)
                return@submit
            } catch (e: Throwable) {
                LOG.error("failed to load image", e)
                javaFxThread { view.showAlert("Failed to load image. Try using other survey source.") }
                task.completeExceptionally(e)
                return@submit
            }

            savePreferences()

            imagePath.get()?.deleteIfExists()
            val tmpFile = Files.createTempFile("framing", ".jpg")
            tmpFile.writeBytes(data)
            imagePath.set(tmpFile)

            javaFxThread {
                val window = imageWindow.get()?.also { it.open(tmpFile.toFile()) }
                    ?: imageWindowOpener.open(tmpFile.toFile())
                imageWindow.set(window)
                task.complete(tmpFile)
            }
        }

        return task
            .whenComplete { _, _ -> loading.set(false) }
    }

    fun populateHipsSurveys() {
        systemExecutorService.submit {
            val data = objectMapper.readValue(resource("data/HIPS_SURVEY_SOURCES.json")!!, Array<HipsSurvey>::class.java)
            val hipsSurveyId = preferences.string("framing.hipsSurvey") ?: DEFAULT_HIPS_SURVEY
            val selected = data.firstOrNull { it.id == hipsSurveyId }
            javaFxThread { view.populateHipsSurveys(data.toList(), selected) }
        }
    }

    fun loadPreferences() {
        preferences.double("framing.fov")?.let { view.updateFOV(it.rad) }
        preferences.double("framing.screen.x")?.let { view.x = it }
        preferences.double("framing.screen.y")?.let { view.y = it }
    }

    fun savePreferences() {
        preferences.string("framing.hipsSurvey", view.hipsSurvey?.id)
        preferences.double("framing.fov", view.frameFOV.value)
        preferences.double("framing.screen.x", view.x)
        preferences.double("framing.screen.y", view.y)
    }

    override fun close() {
        savePreferences()

        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)

        imagePath.getAndSet(null)?.deleteIfExists()

        framingReloader.interrupt()
    }

    private inner class FramingReloader : Thread("Framing Reloader") {

        @JvmField val coordinate = AtomicReference<PairOfAngle>()

        init {
            isDaemon = true
        }

        override fun run() {
            var prevCoordinate: PairOfAngle? = null
            var lastTime = 0L

            try {
                while (true) {
                    val coordinate = coordinate.get()

                    if (coordinate == null
                        || coordinate == prevCoordinate
                        || System.currentTimeMillis() - lastTime < 15000L
                    ) {
                        sleep(1000L)
                    } else {
                        this.coordinate.set(null)
                        LOG.info("starting framing reload. ra={}, dec={}", coordinate.first.hours, coordinate.second.degrees)
                        load(coordinate.first, coordinate.second)?.get()
                        prevCoordinate = coordinate
                        lastTime = System.currentTimeMillis()
                    }
                }
            } catch (e: InterruptedException) {
                // empty.
            } catch (e: Throwable) {
                LOG.error("framing realod thread error", e)
            }
        }
    }

    companion object {

        const val DEFAULT_HIPS_SURVEY = "CDS/P/DSS2/color"

        @JvmStatic private val LOG = LoggerFactory.getLogger(FramingManager::class.java)
    }
}
