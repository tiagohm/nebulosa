package nebulosa.desktop.logic.framing

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.disposables.Disposable
import jakarta.annotation.PostConstruct
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.framing.FramingView
import nebulosa.erfa.PairOfAngle
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.indi.device.mount.MountEquatorialCoordinatesChanged
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
    private val loadingLatch = CountUpDownLatch()
    private val framingReloader = FramingReloader()

    val loading = SimpleBooleanProperty()

    val mount
        get() = equipmentManager.selectedMount

    @PostConstruct
    private fun initialize() {
        subscribers[0] = deviceEventBus
            .filter { it is MountEquatorialCoordinatesChanged && it.device === equipmentManager.selectedMount.value }
            .cast(MountEquatorialCoordinatesChanged::class.java)
            .subscribe(::onMountEquatorialCoordinatesChanged)

        framingReloader.start()
    }

    private fun onMountEquatorialCoordinatesChanged(event: MountEquatorialCoordinatesChanged) {
        if (view.syncFromMount) {
            javaFxThread {
                val coordinate = PairOfAngle(event.device.rightAscensionJ2000, event.device.declinationJ2000)
                view.updateCoordinate(coordinate.first, coordinate.second)
                framingReloader.coordinate.set(coordinate)
            }
        }
    }

    @Synchronized
    fun load(ra: Angle, dec: Angle) {
        if (loading.get()) {
            LOG.warn("framing is already loading")
            return
        }

        loading.set(true)
        loadingLatch.countUp()

        systemExecutorService.submit {
            val data = try {
                while (view.hipsSurvey == null) {
                    Thread.sleep(100L)
                }

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
                return@submit
            } catch (e: Throwable) {
                LOG.error("failed to load image", e)
                javaFxThread { view.showAlert("Failed to load image. Try using other survey source.") }
                return@submit
            } finally {
                loading.set(false)
                loadingLatch.countDown()
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
            }
        }
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
            try {
                while (true) {
                    val coordinate = coordinate.getAndSet(null)

                    if (coordinate == null) {
                        sleep(5000L)
                    } else {
                        LOG.info("starting framing reload. ra={}, dec={}", coordinate.first.hours, coordinate.second.degrees)
                        loadingLatch.await()
                        load(coordinate.first, coordinate.second)
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
