package nebulosa.api.services

import nebulosa.api.data.enums.HipsSurveyType
import nebulosa.fits.FITS_DEC_ANGLE_FORMATTER
import nebulosa.fits.FITS_RA_ANGLE_FORMATTER
import nebulosa.hips2fits.FormatOutputType
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import nom.tam.fits.header.ObservationDescription
import nom.tam.fits.header.Standard
import nom.tam.fits.header.extra.MaxImDLExt
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import kotlin.math.max

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = Angle.ZERO, hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Pair<Image, Calibration>? {
        val data = hips2FitsService.query(
            hipsSurveyType.hipsSurvey,
            rightAscension, declination,
            width, height,
            rotation, fov,
            format = FormatOutputType.JPG,
        ).execute().body() ?: return null

        val image = Image.open(ByteArrayInputStream(data))

        image.header.addValue(Standard.INSTRUME, hipsSurveyType.hipsSurvey.id)
        image.header.addValue(ObservationDescription.RA, rightAscension.format(FITS_RA_ANGLE_FORMATTER))
        image.header.addValue(ObservationDescription.DEC, declination.format(FITS_DEC_ANGLE_FORMATTER))
        image.header.addValue(MaxImDLExt.ROTATANG, rotation.degrees)
        image.header.addValue("COMMENT", null as String?, "Made use of hips2fits, a service provided by CDS.")

        val crot = -rotation + Angle.SEMICIRCLE
        val cdelt = fov / max(width, height)

        val calibration = Calibration(
            hasWCS = true,
            ctype1 = "RA---TAN", ctype2 = "DEC--TAN",
            crpix1 = width / 2.0, crpix2 = height / 2.0,
            crval1 = rightAscension, crval2 = declination,
            cdelt1 = cdelt, cdelt2 = cdelt,
            crota1 = crot, crota2 = crot,
            width = cdelt * width,
            height = cdelt * height,
        )

        return image to calibration
    }
}
