package nebulosa.api.services

import nebulosa.api.data.enums.HipsSurveyType
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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeBytes
import kotlin.math.abs

@Service
class FramingService(private val hips2FitsService: Hips2FitsService) {

    fun frame(
        rightAscension: Angle, declination: Angle,
        width: Int, height: Int, fov: Angle,
        rotation: Angle = Angle.ZERO, hipsSurveyType: HipsSurveyType = HipsSurveyType.CDS_P_DSS2_COLOR,
    ): Triple<Image, Path, Calibration>? {
        val data = hips2FitsService.query(
            hipsSurveyType.hipsSurvey,
            rightAscension, declination,
            width, height,
            rotation, fov,
            format = FormatOutputType.FITS,
        ).execute().body() ?: return null

        val fitsPath = Files.createTempFile("framing", ".fits")
        fitsPath.writeBytes(data)
        LOG.info("framing file saved. path={}", fitsPath)

        val image = Image.open(fitsPath.toFile())

        // val crot = -rotation + Angle.SEMICIRCLE
        val cdelt1 = image.header.getDoubleValue(FitsKeywords.CDELT1).deg
        val cdelt2 = image.header.getDoubleValue(FitsKeywords.CDELT2).deg

        val calibration = Calibration(
            hasWCS = true,
            ctype1 = image.header.getStringValue(FitsKeywords.CTYPE1),
            ctype2 = image.header.getStringValue(FitsKeywords.CTYPE2),
            crpix1 = image.header.getDoubleValue(FitsKeywords.CRPIX1),
            crpix2 = image.header.getDoubleValue(FitsKeywords.CRPIX2),
            crval1 = image.header.getDoubleValue(FitsKeywords.CRVAL1).deg,
            crval2 = image.header.getDoubleValue(FitsKeywords.CRVAL2).deg,
            cdelt1 = cdelt1,
            cdelt2 = cdelt2,
            crota1 = image.header.getDoubleValue(FitsKeywords.CROTA1).deg,
            crota2 = image.header.getDoubleValue(FitsKeywords.CROTA2).deg,
            width = abs(cdelt1.value).rad * width,
            height = abs(cdelt2.value).rad * height,
        )

        return Triple(image, fitsPath, calibration)
    }

    companion object {

        @JvmStatic
        private val LOG = loggerFor<FramingService>()
    }
}
