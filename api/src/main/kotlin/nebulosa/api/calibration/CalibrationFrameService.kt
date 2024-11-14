package nebulosa.api.calibration

import nebulosa.fits.*
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.correction.BiasSubtraction
import nebulosa.image.algorithms.transformation.correction.DarkSubtraction
import nebulosa.image.algorithms.transformation.correction.FlatCorrection
import nebulosa.image.format.ImageHdu
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.camera.FrameType.Companion.frameType
import nebulosa.log.loggerFor
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.math.abs
import kotlin.math.roundToInt

// https://cdn.diffractionlimited.com/help/maximdl/Understanding_Calibration_Groups.htm
// https://www.astropy.org/ccd-reduction-and-photometry-guide/v/dev/notebooks/00-00-Preface.html

class CalibrationFrameService(private val calibrationFrameRepository: CalibrationFrameRepository) : CalibrationFrameProvider {

    fun calibrate(group: String, image: Image, createNew: Boolean = false): Image {
        return synchronized(image) {
            val darkFrame = findBestDarkFrames(group, image).firstOrNull()
            val biasFrame = if (darkFrame == null) findBestBiasFrames(group, image).firstOrNull() else null
            val flatFrame = findBestFlatFrames(group, image).firstOrNull()

            val darkImage = darkFrame?.path?.fits()?.use(Image::open)
            val biasImage = biasFrame?.path?.fits()?.use(Image::open)
            var flatImage = flatFrame?.path?.fits()?.use(Image::open)

            if (darkImage != null || biasImage != null || flatImage != null) {
                var transformedImage = if (createNew) image.clone() else image

                // If not using dark frames.
                if (biasImage != null) {
                    // Subtract Master Bias from Flat Frames.
                    if (flatImage != null) {
                        flatImage = flatImage.transform(BiasSubtraction(biasImage))
                        LOG.info("bias frame subtraction applied to flat frame. frame={}", biasFrame)
                    }

                    // Subtract the Master Bias frame.
                    transformedImage = transformedImage.transform(BiasSubtraction(biasImage))
                    LOG.info("bias frame subtraction applied. frame={}", biasFrame)
                } else if (darkFrame == null) {
                    LOG.warn("no bias frames found. width={}, height={}, bin={}, gain={}", image.width, image.height, image.header.binX, image.header.gain)
                }

                // Subtract Master Dark frame.
                if (darkImage != null) {
                    transformedImage = transformedImage.transform(DarkSubtraction(darkImage))
                    LOG.info("dark frame subtraction applied. frame={}", darkFrame)
                } else {
                    LOG.warn("no dark frames found. width={}, height={}, bin={}, exposureTime={}, gain={}", image.width, image.height, image.header.binX, image.header.exposureTimeInMicroseconds, image.header.gain)
                }

                // Divide the Dark-subtracted Light frame by the Master Flat frame to correct for variations in the optical path.
                if (flatImage != null) {
                    transformedImage = transformedImage.transform(FlatCorrection(flatImage))
                    LOG.info("flat frame correction applied. frame={}", flatFrame)
                } else {
                    LOG.warn("no flat frames found. filter={}, width={}, height={}, bin={}", image.header.filter, image.width, image.height, image.header.binX)
                }

                transformedImage
            } else {
                LOG.warn("no calibration frames found.  width={}, height={}, bin={}, gain={}, filter={}, exposureTime={}", image.width, image.height, image.header.binX, image.header.gain, image.header.filter, image.header.exposureTimeInMicroseconds)
                image
            }
        }
    }

    fun groups(): Collection<String> {
        return calibrationFrameRepository.groups()
    }

    fun frames(group: String): List<CalibrationFrameEntity> {
        return calibrationFrameRepository.findAll(group)
    }

    fun upload(group: String, path: Path): List<CalibrationFrameEntity> {
        val files = if (path.isRegularFile()) listOf(path)
        else if (path.isDirectory()) path.listDirectoryEntries("*.{fits,fit,xisf}").filter { it.isRegularFile() }
        else return emptyList()

        return upload(group, files)
    }

    @Synchronized
    fun upload(group: String, files: List<Path>): List<CalibrationFrameEntity> {
        val frames = ArrayList<CalibrationFrameEntity>(files.size)

        for (file in files) {
            calibrationFrameRepository.delete(group, "$file")

            try {
                val image = if (file.isFits()) file.fits()
                else if (file.isXisf()) file.xisf()
                else continue

                image.use {
                    val hdu = image.filterIsInstance<ImageHdu>().firstOrNull() ?: return@use
                    val header = hdu.header
                    val frameType = header.frameType?.takeIf { it != FrameType.LIGHT } ?: return@use

                    val exposureTime = if (frameType == FrameType.DARK) header.exposureTimeInMicroseconds else 0L
                    val temperature = if (frameType == FrameType.DARK) header.temperature else INVALID_TEMPERATURE
                    val gain = if (frameType != FrameType.FLAT) header.gain else 0.0
                    val filter = if (frameType == FrameType.FLAT) header.filter else null

                    val frame = CalibrationFrameEntity(
                        0L, frameType, group, filter,
                        exposureTime, temperature,
                        header.width, header.height, header.binX, header.binY,
                        gain, file,
                    )

                    calibrationFrameRepository.add(frame)
                        .also(frames::add)
                }
            } catch (e: Throwable) {
                LOG.error("cannot open FITS. path={}, message={}", file, e.message)
            }
        }

        return frames
    }

    fun edit(frame: CalibrationFrameEntity): CalibrationFrameEntity {
        check(calibrationFrameRepository.update(frame)) { "failed to update entity" }
        return frame
    }

    fun delete(id: Long) {
        calibrationFrameRepository.delete(id)
    }

    override fun findBestDarkFrames(
        group: String, temperature: Double, width: Int, height: Int,
        binX: Int, binY: Int, exposureTimeInMicroseconds: Long,
        gain: Double,
    ): List<CalibrationFrameEntity> {
        val frames = calibrationFrameRepository
            .darkFrames(group, width, height, binX, exposureTimeInMicroseconds, gain)

        if (frames.isEmpty()) return emptyList()

        // Closest temperature.
        val groupedFrames = TreeMap<Int, MutableList<CalibrationFrameEntity>>()
        frames.groupByTo(groupedFrames) { abs(it.temperature - temperature).roundToInt() }
        // TODO: Dont use if temperature is out of tolerance range
        // TODO: Generate master from matched frames.
        return groupedFrames.firstEntry().value
    }

    fun findBestDarkFrames(group: String, image: Image): List<CalibrationFrameEntity> {
        val header = image.header
        val temperature = header.temperature
        val binX = header.binX
        val exposureTime = header.exposureTimeInMicroseconds

        return findBestDarkFrames(group, temperature, image.width, image.height, binX, binX, exposureTime, header.gain)
    }

    override fun findBestFlatFrames(
        group: String, width: Int, height: Int,
        binX: Int, binY: Int, filter: String?
    ): List<CalibrationFrameEntity> {
        // TODO: Generate master from matched frames. (Subtract the master bias frame from each flat frame)
        return calibrationFrameRepository
            .flatFrames(group, filter, width, height, binX)
    }

    fun findBestFlatFrames(group: String, image: Image): List<CalibrationFrameEntity> {
        val header = image.header
        val filter = header.filter
        val binX = header.binX

        return findBestFlatFrames(group, image.width, image.height, binX, binX, filter)
    }

    override fun findBestBiasFrames(
        group: String, width: Int, height: Int,
        binX: Int, binY: Int, gain: Double,
    ): List<CalibrationFrameEntity> {
        // TODO: Generate master from matched frames.
        return calibrationFrameRepository.biasFrames(group, width, height, binX, gain)
    }

    fun findBestBiasFrames(group: String, image: Image): List<CalibrationFrameEntity> {
        val header = image.header
        val binX = header.binX

        return findBestBiasFrames(group, image.width, image.height, binX, binX, image.header.gain)
    }

    companion object {

        private val LOG = loggerFor<CalibrationFrameService>()
    }
}
