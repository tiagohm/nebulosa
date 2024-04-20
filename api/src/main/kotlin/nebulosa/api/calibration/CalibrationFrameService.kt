package nebulosa.api.calibration

import nebulosa.fits.*
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.correction.BiasSubtraction
import nebulosa.image.algorithms.transformation.correction.DarkSubtraction
import nebulosa.image.algorithms.transformation.correction.FlatCorrection
import nebulosa.image.format.ImageHdu
import nebulosa.image.format.ReadableHeader
import nebulosa.indi.device.camera.FrameType
import nebulosa.log.loggerFor
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.math.abs
import kotlin.math.roundToInt

// https://cdn.diffractionlimited.com/help/maximdl/Understanding_Calibration_Groups.htm
// https://www.astropy.org/ccd-reduction-and-photometry-guide/v/dev/notebooks/00-00-Preface.html

@Service
class CalibrationFrameService(
    private val calibrationFrameRepository: CalibrationFrameRepository,
) {

    fun calibrate(name: String, image: Image, createNew: Boolean = false): Image {
        return synchronized(image) {
            val darkFrame = findBestDarkFrames(name, image).firstOrNull()
            val biasFrame = findBestBiasFrames(name, image).firstOrNull()
            val flatFrame = findBestFlatFrames(name, image).firstOrNull()

            if (darkFrame != null || biasFrame != null || flatFrame != null) {
                var transformedImage = if (createNew) image.clone() else image

                if (biasFrame != null) {
                    val calibrationImage = biasFrame.path!!.fits().use(Image::open)
                    transformedImage = transformedImage.transform(BiasSubtraction(calibrationImage))
                    LOG.info("bias frame subtraction applied. frame={}", biasFrame)
                } else {
                    LOG.info(
                        "no bias frames found. width={}, height={}, bin={}, gain={}",
                        image.width, image.height, image.header.binX, image.header.gain
                    )
                }

                if (darkFrame != null) {
                    val calibrationImage = darkFrame.path!!.fits().use(Image::open)
                    transformedImage = transformedImage.transform(DarkSubtraction(calibrationImage))
                    LOG.info("dark frame subtraction applied. frame={}", darkFrame)
                } else {
                    LOG.info(
                        "no dark frames found. width={}, height={}, bin={}, exposureTime={}, gain={}",
                        image.width, image.height, image.header.binX, image.header.exposureTimeInMicroseconds, image.header.gain
                    )
                }

                if (flatFrame != null) {
                    val calibrationImage = flatFrame.path!!.fits().use(Image::open)
                    transformedImage = transformedImage.transform(FlatCorrection(calibrationImage))
                    LOG.info("flat frame correction applied. frame={}", flatFrame)
                } else {
                    LOG.info(
                        "no flat frames found. filter={}, width={}, height={}, bin={}",
                        image.header.filter, image.width, image.height, image.header.binX
                    )
                }

                transformedImage
            } else {
                LOG.info(
                    "no calibration frames found.  width={}, height={}, bin={}, gain={}, filter={}, exposureTime={}",
                    image.width, image.height, image.header.binX, image.header.gain, image.header.filter, image.header.exposureTimeInMicroseconds
                )
                image
            }
        }
    }

    fun groups() = calibrationFrameRepository.groups()

    fun groupedCalibrationFrames(name: String): Map<CalibrationGroupKey, List<CalibrationFrameEntity>> {
        val frames = calibrationFrameRepository.findAll(name)
        return frames.groupBy(CalibrationGroupKey::from)
    }

    fun upload(name: String, path: Path): List<CalibrationFrameEntity> {
        val files = if (path.isRegularFile() && path.isFits) listOf(path)
        else if (path.isDirectory()) path.listDirectoryEntries("*.{fits,fit,xisf}").filter { it.isRegularFile() }
        else return emptyList()

        return upload(name, files)
    }

    @Synchronized
    fun upload(name: String, files: List<Path>): List<CalibrationFrameEntity> {
        val frames = ArrayList<CalibrationFrameEntity>(files.size)

        for (file in files) {
            calibrationFrameRepository.delete(name, "$file")

            try {
                val image = if (file.isFits()) file.fits()
                else if (file.isXisf()) file.xisf()
                else continue

                image.use {
                    val hdu = image.filterIsInstance<ImageHdu>().firstOrNull() ?: return@use
                    val header = hdu.header
                    val frameType = header.frameType?.takeIf { it != FrameType.LIGHT } ?: return@use

                    val exposureTime = if (frameType == FrameType.DARK) header.exposureTimeInMicroseconds else 0L
                    val temperature = if (frameType == FrameType.DARK) header.temperature else 999.0
                    val gain = if (frameType != FrameType.FLAT) header.gain else 0.0
                    val filter = if (frameType == FrameType.FLAT) header.filter else null

                    val frame = CalibrationFrameEntity(
                        0L, frameType, name, filter,
                        exposureTime, temperature,
                        header.width, header.height, header.binX, header.binY,
                        gain, file,
                    )

                    calibrationFrameRepository.save(frame)
                        .also(frames::add)
                }
            } catch (e: Throwable) {
                LOG.error("cannot open FITS. path={}, message={}", file, e.message)
            }
        }

        return frames
    }

    fun edit(frame: CalibrationFrameEntity, name: String, enabled: Boolean): CalibrationFrameEntity {
        frame.name = name
        frame.enabled = enabled
        return calibrationFrameRepository.save(frame)
    }

    fun delete(frame: CalibrationFrameEntity) {
        calibrationFrameRepository.delete(frame)
    }

    // exposureTime, temperature, width, height, binX, binY, gain.
    fun findBestDarkFrames(name: String, image: Image): List<CalibrationFrameEntity> {
        val header = image.header
        val temperature = header.temperature

        val frames = calibrationFrameRepository
            .darkFrames(name, image.width, image.height, header.binX, header.exposureTimeInMicroseconds, header.gain)

        if (frames.isEmpty()) return emptyList()

        // Closest temperature.
        val groupedFrames = TreeMap<Int, MutableList<CalibrationFrameEntity>>()
        frames.groupByTo(groupedFrames) { abs(it.temperature - temperature).roundToInt() }
        // TODO: Dont use if temperature is out of tolerance range
        // TODO: Generate master from matched frames.
        return groupedFrames.firstEntry().value
    }

    // filter, width, height, binX, binY.
    fun findBestFlatFrames(name: String, image: Image): List<CalibrationFrameEntity> {
        val filter = image.header.filter

        // TODO: Generate master from matched frames.
        return calibrationFrameRepository
            .flatFrames(name, filter, image.width, image.height, image.header.binX)
    }

    // width, height, binX, binY, gain.
    fun findBestBiasFrames(name: String, image: Image): List<CalibrationFrameEntity> {
        // TODO: Generate master from matched frames.
        return calibrationFrameRepository
            .biasFrames(name, image.width, image.height, image.header.binX, image.header.gain)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CalibrationFrameService>()

        @JvmStatic val ReadableHeader.frameType
            get() = frame?.uppercase()?.let {
                if ("LIGHT" in it) FrameType.LIGHT
                else if ("DARK" in it) FrameType.DARK
                else if ("FLAT" in it) FrameType.FLAT
                else if ("BIAS" in it) FrameType.BIAS
                else null
            }

        inline val Path.isFits
            get() = "$this".let { it.endsWith(".fits") || it.endsWith(".fit") }
    }
}
