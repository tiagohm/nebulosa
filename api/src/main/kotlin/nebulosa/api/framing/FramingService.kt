package nebulosa.api.framing

import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = Angle.ZERO,
        hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Pair<Image, Calibration?>? {
        val data = hips2FitsService.query(
            hipsSurveyType.hipsSurvey,
            rightAscension, declination,
            width, height,
            rotation, fov,
            format = FormatOutputType.FITS,
        ).execute().body() ?: return null

        val image = Image.openFITS(ByteArrayInputStream(data))
        val calibration = Calibration.from(image.header)
        LOG.info("framing file loaded. calibration={}", calibration)
        return image to calibration
    }

    companion object {

        @JvmStatic
        private val LOG = loggerFor<FramingService>()
    }
}
