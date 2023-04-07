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

    fun populateHipsSurveys() {
        val hipsSurveyId = preferences.string("framing.hipsSurvey") ?: DEFAULT_HIPS_SURVEY
        val selected = HIPS_SURVEY_SOURCES.firstOrNull { it.id == hipsSurveyId }
        view.populateHipsSurveys(HIPS_SURVEY_SOURCES, selected)
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

        @JvmStatic private val HIPS_SURVEY_SOURCES = listOf(
            HipsSurvey("CDS/P/DSS2/NIR", "Image/Optical/DSS", "equatorial", "Optical", 16, 2.236E-4, 0.9955),
            HipsSurvey("CDS/P/DSS2/blue", "Image/Optical/DSS", "equatorial", "Optical", 16, 2.236E-4, 0.9972),
            HipsSurvey("CDS/P/DSS2/color", "Image/Optical/DSS", "equatorial", "Optical", 0, 2.236E-4, 1.0),
            HipsSurvey("CDS/P/DSS2/red", "Image/Optical/DSS", "equatorial", "Optical", 16, 2.236E-4, 1.0),
            HipsSurvey("fzu.cz/P/CTA-FRAM/survey/B", "Image/Optical/CTA-FRAM", "equatorial", "Optical", -64, 0.003579, 1.0),
            HipsSurvey("fzu.cz/P/CTA-FRAM/survey/R", "Image/Optical/CTA-FRAM", "equatorial", "Optical", -64, 0.003579, 1.0),
            HipsSurvey("fzu.cz/P/CTA-FRAM/survey/V", "Image/Optical/CTA-FRAM", "equatorial", "Optical", -64, 0.003579, 1.0),
            HipsSurvey("fzu.cz/P/CTA-FRAM/survey/color", "Image/Optical/CTA-FRAM", "equatorial", "Optical", 0, 0.003579, 1.0),
            HipsSurvey("CDS/P/2MASS/H", "Image/Infrared/2MASS", "equatorial", "Infrared", -32, 2.236E-4, 1.0),
            HipsSurvey("CDS/P/2MASS/J", "Image/Infrared/2MASS", "equatorial", "Infrared", -32, 2.236E-4, 1.0),
            HipsSurvey("CDS/P/2MASS/K", "Image/Infrared/2MASS", "equatorial", "Infrared", -32, 2.236E-4, 1.0),
            HipsSurvey("CDS/P/2MASS/color", "Image/Infrared/2MASS", "equatorial", "Infrared", 0, 2.236E-4, 1.0),
            HipsSurvey("CDS/P/AKARI/FIS/Color", "Image/Infrared/AKARI-FIS", "equatorial", "Infrared", 0, 0.003579, 1.0),
            HipsSurvey("CDS/P/AKARI/FIS/N160", "Image/Infrared/AKARI-FIS", "equatorial", "Infrared", -32, 0.003579, 1.0),
            HipsSurvey("CDS/P/AKARI/FIS/N60", "Image/Infrared/AKARI-FIS", "equatorial", "Infrared", -32, 0.003579, 1.0),
            HipsSurvey("CDS/P/AKARI/FIS/WideL", "Image/Infrared/AKARI-FIS", "equatorial", "Infrared", -32, 0.003579, 1.0),
            HipsSurvey("CDS/P/AKARI/FIS/WideS", "Image/Infrared/AKARI-FIS", "equatorial", "Infrared", -32, 0.003579, 1.0),
            HipsSurvey("CDS/P/NEOWISER/Color", "Image/Infrared/WISE/NEOWISER", "equatorial", "Infrared", 0, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/NEOWISER/W1", "Image/Infrared/WISE/NEOWISER", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/NEOWISER/W2", "Image/Infrared/WISE/NEOWISER", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/WISE/WSSA/12um", "Image/Infrared/WISE/WSSA", "equatorial", "Infrared", -32, 8.946E-4, 1.0),
            HipsSurvey("CDS/P/allWISE/W1", "Image/Infrared/WISE", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/allWISE/W2", "Image/Infrared/WISE", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/allWISE/W3", "Image/Infrared/WISE", "equatorial", "Infrared", -32, 4.473E-4, 0.9999),
            HipsSurvey("CDS/P/allWISE/W4", "Image/Infrared/WISE", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/allWISE/color", "Image/Infrared/WISE", "equatorial", "Infrared", 0, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/unWISE/W1", "Image/Infrared/WISE/unWISE", "equatorial", "Infrared", -32, 0.229, 1.0),
            HipsSurvey("CDS/P/unWISE/W2", "Image/Infrared/WISE/unWISE", "equatorial", "Infrared", -32, 0.229, 1.0),
            HipsSurvey("CDS/P/unWISE/color-W2-W1W2-W1", "Image/Infrared/WISE/unWISE", "equatorial", "Infrared", -32, 4.473E-4, 1.0),
            HipsSurvey("CDS/P/RASS", "Image/X/ROSAT", "equatorial", "X-ray", 16, 0.007157, 1.0),
            HipsSurvey("JAXA/P/ASCA_GIS", "Image/X/ASCA", "equatorial", "X-ray", 0, 0.001789, 1.0),
            HipsSurvey("JAXA/P/ASCA_SIS", "Image/X/ASCA", "equatorial", "X-ray", 0, 0.001789, 1.0),
            HipsSurvey("JAXA/P/MAXI-GSC", "Image/X/MAXI", "equatorial", "X-ray", 0, 0.001789, 1.0),
            HipsSurvey("JAXA/P/MAXI-SSC", "Image/X/MAXI", "equatorial", "X-ray", 0, 0.1145, 1.0),
            HipsSurvey("JAXA/P/SUZAKU", "Image/X", "equatorial", "X-ray", 0, 0.001789, 1.0),
            HipsSurvey("JAXA/P/SWIFT_BAT_FLUX", "Image/X", "equatorial", "X-ray", 0, 0.001789, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/100-150", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/1000-2000", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/150-300", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/2000-4000", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/30-50", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/300-500", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/4000-10000", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/50-70", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/500-1000", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/Dif/70-100", "Image/Gamma-ray/EGRET/Diffuse", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/inf100", "Image/Gamma-ray/EGRET", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/EGRET/sup100", "Image/Gamma-ray/EGRET", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/Fermi/3", "Image/Gamma-ray", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/Fermi/4", "Image/Gamma-ray", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/Fermi/5", "Image/Gamma-ray", "equatorial", "Gamma-ray", -32, 0.01431, 1.0),
            HipsSurvey("CDS/P/Fermi/color", "Image/Gamma-ray", "equatorial", "Gamma-ray", 0, 0.01431, 1.0),
        )
    }
}
