package nebulosa.api.framing

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.fits.fits
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.image.Image
import nebulosa.io.resource
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.platesolver.PlateSolution
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

class FramingService(
    private val hips2FitsService: Hips2FitsService,
    private val objectMapper: ObjectMapper,
) {

    val availableHipsSurveys by lazy {
        resource("HIPS_SURVEYS.json")!!
            .use { objectMapper.readValue(it, Array<HipsSurvey>::class.java) }
            .sorted()
    }

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

        LOG.d { info("framing file loaded. calibration={}", solution) }

        return Triple(image, solution, DEFAULT_PATH)
    }

    companion object {

        private val LOG = loggerFor<FramingService>()
        private val DEFAULT_PATH = Files.createTempFile("framing", ".fits")
    }
}
