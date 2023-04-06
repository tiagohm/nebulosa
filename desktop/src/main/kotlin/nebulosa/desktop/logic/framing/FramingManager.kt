package nebulosa.desktop.logic.framing

import com.fasterxml.jackson.databind.ObjectMapper
import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.framing.FramingView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.withIO
import nebulosa.desktop.withMain
import nebulosa.fits.FITS_DEC_ANGLE_FORMATTER
import nebulosa.fits.FITS_RA_ANGLE_FORMATTER
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.imaging.Image
import nebulosa.indi.device.mount.Mount
import nebulosa.io.resource
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nom.tam.fits.header.ObservationDescription
import nom.tam.fits.header.Standard
import nom.tam.fits.header.extra.MaxImDLExt
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InterruptedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes
import kotlin.math.max

@Component
class FramingManager(@Autowired internal val view: FramingView) : Closeable {

    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var hips2FitsService: Hips2FitsService
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var preferences: Preferences

    private val imageView = AtomicReference<ImageView>()
    private val imagePath = AtomicReference<Path>()

    val loading = SimpleBooleanProperty()

    val mount
        get() = equipmentManager.selectedMount

    suspend fun sync(device: Mount? = null) {
        val mount = device ?: equipmentManager.selectedMount.value ?: return
        val coordinate = PairOfAngle(mount.rightAscensionJ2000, mount.declinationJ2000)
        view.updateCoordinate(coordinate.first, coordinate.second)
        load(coordinate.first, coordinate.second)
    }

    suspend fun load(rightAscension: Angle, declination: Angle): Path? {
        if (loading.get()) {
            LOG.warn("framing is already loading")
            return null
        }

        if (!view.showing) {
            LOG.warn("framing window is hidden")
            return null
        }

        withMain { loading.set(true) }

        val rotation = view.frameRotation
        lateinit var hipsSurvey: HipsSurvey
        var path: Path? = null

        withIO {
            val data = try {
                hipsSurvey = view.hipsSurvey!!

                LOG.info(
                    "loading image. survey={}, ra={}, dec={}, width={}, height={}, rotation={}",
                    hipsSurvey, rightAscension.hours, declination.degrees,
                    view.frameWidth, view.frameHeight, rotation.degrees,
                )

                hips2FitsService.query(
                    hipsSurvey,
                    view.frameRA, view.frameDEC,
                    view.frameWidth, view.frameHeight, rotation,
                    view.frameFOV,
                    format = FormatOutputType.JPG,
                ).execute().body()!!
            } catch (e: InterruptedIOException) {
                view.showAlert("Image took a long time to load. Please try again.")
                return@withIO
            } catch (e: Throwable) {
                LOG.error("failed to load image", e)
                view.showAlert("Failed to load image. Try using other survey source.")
                return@withIO
            }

            savePreferences()

            imagePath.get()?.deleteIfExists()
            path = Files.createTempFile("framing", ".jpg")
            path!!.writeBytes(data)
            imagePath.set(path)

            val image = Image.open(ByteArrayInputStream(data))

            image.header.addValue(Standard.INSTRUME, hipsSurvey.id)
            image.header.addValue(ObservationDescription.RA, rightAscension.format(FITS_RA_ANGLE_FORMATTER))
            image.header.addValue(ObservationDescription.DEC, declination.format(FITS_DEC_ANGLE_FORMATTER))
            image.header.addValue(MaxImDLExt.ROTATANG, rotation.degrees)
            image.header.addValue("COMMENT", null as String?, "Made use of hips2fits, a service provided by CDS.")

            val currentImageView = imageView.get()

            withMain {
                try {
                    if (currentImageView != null) {
                        currentImageView.open(image, path!!.toFile(), resetTransformation = true)
                        currentImageView.show(requestFocus = true)
                    } else {
                        val window = imageViewOpener.open(image, path!!.toFile(), resetTransformation = true)
                        imageView.set(window)
                    }
                } catch (e: Throwable) {
                    LOG.error("image open failed", e)
                }
            }
        }

        withMain { loading.set(false) }

        return path
    }

    suspend fun populateHipsSurveys() = withIO {
        val data = objectMapper.readValue(resource("data/HIPS_SURVEY_SOURCES.json")!!, Array<HipsSurvey>::class.java)
        val hipsSurveyId = preferences.string("framing.hipsSurvey") ?: DEFAULT_HIPS_SURVEY
        val selected = data.firstOrNull { it.id == hipsSurveyId }
        view.populateHipsSurveys(data.toList(), selected)
    }

    suspend fun loadPreferences() {
        preferences.double("framing.fov")?.let { view.updateFOV(it.rad) }
        preferences.double("framing.screen.x")?.let { view.x = it }
        preferences.double("framing.screen.y")?.let { view.y = it }
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferences.string("framing.hipsSurvey", view.hipsSurvey?.id)
        preferences.double("framing.fov", view.frameFOV.value)
        preferences.double("framing.screen.x", max(0.0, view.x))
        preferences.double("framing.screen.y", max(0.0, view.y))
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
