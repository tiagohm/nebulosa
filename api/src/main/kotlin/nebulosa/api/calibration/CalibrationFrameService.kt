package nebulosa.api.calibration

import nebulosa.fits.*
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.transformation.correction.BiasSubtraction
import nebulosa.imaging.algorithms.transformation.correction.DarkSubtraction
import nebulosa.imaging.algorithms.transformation.correction.FlatCorrection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
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
        val biasFrame = findBestBiasFrames(camera, image).firstOrNull()
        val flatFrame = findBestFlatFrames(camera, image).firstOrNull()

        return if (darkFrame != null || biasFrame != null || flatFrame != null) {
            var transformedImage = if (createNew) image.clone() else image
            var calibrationImage = Image(transformedImage.width, transformedImage.height, Header(), transformedImage.mono)

            if (biasFrame != null) {
                calibrationImage = Fits(biasFrame.path!!).also(Fits::read).use(calibrationImage::load)
                transformedImage = transformedImage.transform(BiasSubtraction(calibrationImage))
                LOG.info("bias frame subtraction applied. frame={}", biasFrame)
            }

            if (darkFrame != null) {
                calibrationImage = Fits(darkFrame.path!!).also(Fits::read).use(calibrationImage::load)
                transformedImage = transformedImage.transform(DarkSubtraction(calibrationImage))
                LOG.info("dark frame subtraction applied. frame={}", darkFrame)
            }

            if (flatFrame != null) {
                calibrationImage = Fits(flatFrame.path!!).also(Fits::read).use(calibrationImage::load)
                transformedImage = transformedImage.transform(FlatCorrection(calibrationImage))
                LOG.info("flat frame correction applied. frame={}", flatFrame)
            }

            transformedImage
        } else {
            image
        }
    }

    fun groupedCalibrationFrames(): Map<CalibrationGroupKey, List<CalibrationFrameEntity>> {
        val frames = calibrationFrameRepository.findAll()
        return frames.groupBy { CalibrationGroupKey.from(it) }
    }

    // exposureTime, temperature, width, height, binX, binY, gain.
    fun findBestDarkFrames(camera: Camera, image: Image, temperatureTolerance: Double = 0.5): List<CalibrationFrameEntity> {
        require(temperatureTolerance in 0.5..10.0) { "temperature tolerance must be between 0.5 and 10" }

        val header = image.header
        val temperature = header.temperature

        val frames = calibrationFrameRepository
            .darkFrames(camera, image.width, image.height, header.binX, header.exposureTimeInMicroseconds, header.gain)

        if (frames.isEmpty()) return emptyList()

        // Closest temperature.
        val groupedFrames = TreeMap<Int, MutableList<CalibrationFrameEntity>>()
        frames.groupByTo(groupedFrames) { (abs(it.temperature - temperature) * temperatureTolerance * 100.0).toInt() }
        // TODO: Generate master from matched frames.
        return groupedFrames.firstEntry().value
    }

    // filter, width, height, binX, binY.
    fun findBestFlatFrames(camera: Camera, image: Image): List<CalibrationFrameEntity> {
        val filter = image.header.filter ?: return emptyList()

        // TODO: Generate master from matched frames.
        return calibrationFrameRepository
            .flatFrames(camera, filter, image.width, image.height, image.header.binX)
    }

    // width, height, binX, binY, gain.
    fun findBestBiasFrames(camera: Camera, image: Image): List<CalibrationFrameEntity> {
        // TODO: Generate master from matched frames.
        return calibrationFrameRepository
            .biasFrames(camera, image.width, image.height, image.header.binX, image.header.gain)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CalibrationFrameService>()

        @JvmStatic val Header.frameType
            get() = frame?.uppercase()?.let {
                if ("LIGHT" in it) FrameType.LIGHT
                else if ("DARK" in it) FrameType.DARK
                else if ("FLAT" in it) FrameType.FLAT
                else if ("BIAS" in it) FrameType.BIAS
                else null
            }
    }
}
