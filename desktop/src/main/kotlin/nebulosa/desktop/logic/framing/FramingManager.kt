package nebulosa.desktop.logic.framing

import com.fasterxml.jackson.databind.ObjectMapper
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.framing.FramingView
import nebulosa.erfa.PairOfAngle
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.imaging.Image
import nebulosa.indi.device.mount.Mount
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
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
    @Autowired private lateinit var hips2FitsService: Hips2FitsService
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var imageWindowOpener: ImageWindow.Opener
    @Autowired private lateinit var preferences: Preferences

    private val imageWindow = AtomicReference<ImageWindow>()
    private val imagePath = AtomicReference<Path>()

    val loading = SimpleBooleanProperty()

    val mount
        get() = equipmentManager.selectedMount

    fun syncFromMount(device: Mount? = null) {
        val mount = device ?: equipmentManager.selectedMount.value ?: return
        mount.computeCoordinates(true, false)
        val coordinate = PairOfAngle(mount.rightAscensionJ2000, mount.declinationJ2000)
        javaFXExecutorService.execute { view.updateCoordinate(coordinate.first, coordinate.second) }
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
                javaFXExecutorService.execute { view.showAlert("Image took a long time to load. Please try again.") }
                task.completeExceptionally(e)
                return@submit
            } catch (e: Throwable) {
                LOG.error("failed to load image", e)
                javaFXExecutorService.execute { view.showAlert("Failed to load image. Try using other survey source.") }
                task.completeExceptionally(e)
                return@submit
            }

            savePreferences()

            imagePath.get()?.deleteIfExists()
            val tmpFile = Files.createTempFile("framing", ".jpg")
            tmpFile.writeBytes(data)
            imagePath.set(tmpFile)

            javaFXExecutorService.execute {
                val image = Image.open(ByteArrayInputStream(data))

                val window = imageWindow.get()?.also { it.open(image, tmpFile.toFile()) }
                    ?: imageWindowOpener.open(image, tmpFile.toFile())
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
            javaFXExecutorService.execute { view.populateHipsSurveys(data.toList(), selected) }
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

        imagePath.getAndSet(null)?.deleteIfExists()
    }

    companion object {

        const val DEFAULT_HIPS_SURVEY = "CDS/P/DSS2/color"

        @JvmStatic private val LOG = LoggerFactory.getLogger(FramingManager::class.java)
    }
}
