package nebulosa.api.framing

import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeBytes

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = 0.0,
        hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Triple<Image, Calibration?, Path>? {
        val data = hips2FitsService.query(
            hipsSurveyType.hipsSurvey,
            rightAscension, declination,
            width, height,
            rotation, fov,
            format = FormatOutputType.FITS,
        ).execute().body() ?: return null

        DEFAULT_PATH.writeBytes(data)
        val image = Image.open(ByteArrayInputStream(data))
        val calibration = Calibration.from(image.header)
        LOG.info("framing file loaded. calibration={}", calibration)
        return Triple(image, calibration, DEFAULT_PATH)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FramingService>()
        @JvmStatic private val DEFAULT_PATH = Files.createTempFile("framing", ".fits")
    }
}
