package nebulosa.api.framing

import nebulosa.fits.FitsKeywords
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.platesolving.Calibration
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import kotlin.math.abs

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = Angle.ZERO,
        hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Pair<Image, Calibration>? {
        val data = hips2FitsService.query(
            hipsSurveyType.hipsSurvey,
            rightAscension, declination,
            width, height,
            rotation, fov,
            format = FormatOutputType.FITS,
        ).execute().body() ?: return null

        val image = Image.openFITS(ByteArrayInputStream(data))

        // val crot = -rotation + Angle.SEMICIRCLE
        val crota2 = image.header.getDoubleValue(FitsKeywords.CROTA2).deg
        val cdelt1 = image.header.getDoubleValue(FitsKeywords.CDELT1).deg
        val cdelt2 = image.header.getDoubleValue(FitsKeywords.CDELT2).deg
        val crval1 = image.header.getDoubleValue(FitsKeywords.CRVAL1).deg
        val crval2 = image.header.getDoubleValue(FitsKeywords.CRVAL2).deg

        val calibration = Calibration(
            true, crota2, cdelt2, crval1, crval2,
            abs(cdelt1.value).rad * width,
            abs(cdelt2.value).rad * height,
        )

        image.header.iterator().forEach(calibration::addLine)

        LOG.info("framing file loaded. calibration={}", calibration)

        return image to calibration
    }

    companion object {

        @JvmStatic
        private val LOG = loggerFor<FramingService>()
    }
}
