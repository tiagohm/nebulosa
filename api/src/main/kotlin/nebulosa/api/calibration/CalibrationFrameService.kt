package nebulosa.api.calibration

import nebulosa.fits.*
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.BiasSubtraction
import nebulosa.imaging.algorithms.DarkSubtraction
import nebulosa.imaging.algorithms.FlatCorrection
import nebulosa.indi.device.camera.Camera
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.abs

// https://cdn.diffractionlimited.com/help/maximdl/Understanding_Calibration_Groups.htm
// https://www.astropy.org/ccd-reduction-and-photometry-guide/v/dev/notebooks/00-00-Preface.html

@Service
class CalibrationFrameService(
    private val calibrationFrameRepository: CalibrationFrameRepository,
) {

    fun calibrate(camera: Camera, image: Image, createNew: Boolean = false): Image {
        val darkFrame = findBestDarkFrames(camera, image).firstOrNull()
        val biasFrame = calibrationFrameRepository.biasFrames(camera, image.width, image.height, image.header.binX).firstOrNull()
        val flatFrame = findBestFlatFrames(camera, image).firstOrNull()

        return if (darkFrame != null || biasFrame != null || flatFrame != null) {
            var transformedImage = if (createNew) image.clone() else image
            var calibrationImage = Image(transformedImage.width, transformedImage.height, Header(), transformedImage.mono)

            if (biasFrame != null) {
                calibrationImage = Fits(biasFrame.path!!).also(Fits::read).filterIsInstance<ImageHdu>().first().let(calibrationImage::load)
                transformedImage = transformedImage.transform(BiasSubtraction(calibrationImage))
                LOG.info("bias calibrated. frame={}", biasFrame)
            }

            if (darkFrame != null) {
                calibrationImage = Fits(darkFrame.path!!).also(Fits::read).filterIsInstance<ImageHdu>().first().let(calibrationImage::load)
                transformedImage = transformedImage.transform(DarkSubtraction(calibrationImage))
                LOG.info("dark calibrated. frame={}", darkFrame)
            }

            if (flatFrame != null) {
                calibrationImage = Fits(flatFrame.path!!).also(Fits::read).filterIsInstance<ImageHdu>().first().let(calibrationImage::load)
                transformedImage = transformedImage.transform(FlatCorrection(calibrationImage))
                LOG.info("flat calibrated. frame={}", flatFrame)
            }

            transformedImage
        } else {
            image
        }
    }

    // TODO: FROM THE BEST DARK FRAMES GENERATE MASTER DARK (CONCAT ORDERED ENTITY ID "ID1.ID2.IDn" E MAKE SHA1 HASH)
    fun findBestDarkFrames(camera: Camera, image: Image, temperatureTolerance: Double = 0.5): List<CalibrationFrameEntity> {
        val temperature = image.header.temperature

        val frames = calibrationFrameRepository.darkFrames(
            camera, image.width, image.height,
            image.header.binX, image.header.exposureTime.inWholeMicroseconds
        )

        if (frames.isEmpty()) return emptyList()

        // Matches temperature.
        val matchedFrames = frames.filter { abs(it.temperature - temperature) <= temperatureTolerance }
        if (matchedFrames.isNotEmpty()) return matchedFrames

        // Closest temperature.
        val groupedFrames = TreeMap<Int, MutableList<CalibrationFrameEntity>>()

        for (frame in frames) {
            val delta = (abs(frame.temperature - temperature) / temperatureTolerance).toInt()
            groupedFrames.getOrPut(delta) { ArrayList() }.add(frame)
        }

        // TODO: RETURN THE HASHED MASTER DARK IF EXISTS OR GENERATE IT.
        return groupedFrames.firstEntry().value
    }

    fun findBestFlatFrames(camera: Camera, image: Image): List<CalibrationFrameEntity> {
        val filter = image.header.getString(Standard.FILTER, "")
            .trim().ifBlank { null }
            ?: return emptyList()

        return calibrationFrameRepository
            .flatFrames(camera, filter, image.width, image.height, image.header.binX)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CalibrationFrameService>()
    }
}
