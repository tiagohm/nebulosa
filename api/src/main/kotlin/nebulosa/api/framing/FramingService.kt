package nebulosa.api.framing

import nebulosa.fits.fits
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.image.Image
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.plate.solving.PlateSolution
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    val availableHipsSurveys by lazy { hips2FitsService.availableSurveys().execute().body()!!.sorted() }

    @Synchronized
    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = 0.0,
        id: String = "CDS/P/DSS2/COLOR",
    ): Triple<Image, PlateSolution?, Path>? {
        val responseBody = hips2FitsService.query(
            id, rightAscension, declination,
            width, height, rotation, fov,
            format = FormatOutputType.FITS,
        ).execute().body() ?: return null

        responseBody.use { it.byteStream().transferAndCloseOutput(DEFAULT_PATH.outputStream()) }

        val image = DEFAULT_PATH.fits().use(Image::open)
        val solution = PlateSolution.from(image.header)

        LOG.info("framing file loaded. calibration={}", solution)

        return Triple(image, solution, DEFAULT_PATH)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FramingService>()
        @JvmStatic private val DEFAULT_PATH = Files.createTempFile("framing", ".fits")
    }
}
