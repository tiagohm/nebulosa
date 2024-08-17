package nebulosa.api.cameras

import nebulosa.api.calibration.CalibrationFrameProvider
import nebulosa.api.livestacker.LiveStackingRequest
import nebulosa.api.stacker.StackerGroupType
import nebulosa.fits.binX
import nebulosa.fits.binY
import nebulosa.fits.exposureTimeInMicroseconds
import nebulosa.fits.filter
import nebulosa.fits.fits
import nebulosa.fits.gain
import nebulosa.fits.height
import nebulosa.fits.isFits
import nebulosa.fits.temperature
import nebulosa.fits.width
import nebulosa.image.format.ImageHdu
import nebulosa.livestacker.LiveStacker
import nebulosa.log.loggerFor
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import java.nio.file.Files
import java.nio.file.Path
import java.util.EnumMap
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

data class CameraLiveStackingManager(
    private val calibrationFrameProvider: CalibrationFrameProvider? = null,
) : AutoCloseable {

    private val liveStackers = EnumMap<StackerGroupType, LiveStacker>(StackerGroupType::class.java)
    private val workingDirectories = HashSet<Path>()

    @Synchronized
    fun start(request: CameraStartCaptureRequest, path: Path): Boolean {
        if (request.stackerGroupType in liveStackers) {
            return true
        } else if (request.stackerGroupType != StackerGroupType.NONE && request.liveStacking.enabled) {
            try {
                val workingDirectory = Files.createTempDirectory("ls-${request.stackerGroupType}-")
                workingDirectories.add(workingDirectory)

                with(request.liveStacking.processCalibrationGroup(request, path).get(workingDirectory)) {
                    start()
                    liveStackers[request.stackerGroupType] = this
                }

                return true
            } catch (e: Throwable) {
                LOG.error("failed to start live stacking. request={}", request.liveStacking, e)
            }
        }

        return false
    }

    @Synchronized
    fun stack(request: CameraStartCaptureRequest, path: Path?): Path? {
        if (path == null || request.stackerGroupType == StackerGroupType.NONE) return null

        val stackerGroupType = request.stackerGroupType
        val liveStacker = liveStackers[stackerGroupType] ?: return null
        val stacker = liveStacker.stacker

        var stackedPath = liveStacker.add(path)

        if (stacker != null && stackedPath != null) {
            val combinedPath = Path.of("${path.parent}", "STACKED.fits")
            val luminancePath = liveStackers[StackerGroupType.LUMINANCE]?.stackedPath
            val redPath = liveStackers[StackerGroupType.RED]?.stackedPath
            val greenPath = liveStackers[StackerGroupType.GREEN]?.stackedPath
            val bluePath = liveStackers[StackerGroupType.BLUE]?.stackedPath

            if (stackerGroupType.isLRGB && (luminancePath != null || redPath != null || greenPath != null || bluePath != null)) {
                val referencePath = luminancePath ?: redPath ?: greenPath ?: bluePath

                if (referencePath != null) {
                    if (redPath != null && redPath !== referencePath) stacker.align(referencePath, redPath, redPath)
                    if (greenPath != null && greenPath !== referencePath) stacker.align(referencePath, greenPath, greenPath)
                    if (bluePath != null && bluePath !== referencePath) stacker.align(referencePath, bluePath, bluePath)

                    if (stacker.combineLRGB(combinedPath, luminancePath, redPath, greenPath, bluePath)) {
                        stackedPath = combinedPath
                    }
                }
            } else if (luminancePath != null) {
                stacker.align(luminancePath, stackedPath, stackedPath)

                if (stacker.combineLuminance(combinedPath, luminancePath, stackedPath, stackerGroupType == StackerGroupType.MONO)) {
                    stackedPath = combinedPath
                }
            }
        }

        return stackedPath ?: liveStacker.stackedPath
    }

    fun stop(request: CameraStartCaptureRequest) {
        liveStackers[request.stackerGroupType]?.stop()
    }

    override fun close() {
        liveStackers.values.forEach { it.close() }
        liveStackers.clear()

        workingDirectories.forEach { it.deleteRecursively() }
        workingDirectories.clear()
    }

    private fun LiveStackingRequest.processCalibrationGroup(request: CameraStartCaptureRequest, path: Path): LiveStackingRequest {
        return if (calibrationFrameProvider != null && enabled &&
            !request.calibrationGroup.isNullOrBlank() && (darkPath == null || flatPath == null || biasPath == null)
        ) {
            val image = if (path.isFits()) path.fits()
            else if (path.isXisf()) path.xisf()
            else return this

            val hdu = image.use { it.firstOrNull { it is ImageHdu } } ?: return this
            val header = hdu.header
            val temperature = header.temperature
            val binX = header.binX
            val binY = header.binY
            val width = header.width
            val height = header.height
            val exposureTime = header.exposureTimeInMicroseconds
            val gain = header.gain
            val filter = header.filter
            val calibrationGroup = request.calibrationGroup

            LOG.info(
                "find calibration frames for live stacking. group={}, temperature={}, binX={}, binY={}. width={}, height={}, exposureTime={}, gain={}, filter={}",
                calibrationGroup, temperature, binX, binY, width, height, exposureTime, gain, filter
            )

            val newDarkPath = (if (useCalibrationGroup) calibrationFrameProvider
                .findBestDarkFrames(calibrationGroup, temperature, width, height, binX, binY, exposureTime, gain)
                .firstOrNull()?.path else darkPath)?.takeIf { it.isCalibrationFrame }

            val newFlatPath = (if (useCalibrationGroup) calibrationFrameProvider
                .findBestFlatFrames(calibrationGroup, width, height, binX, binY, filter)
                .firstOrNull()?.path else flatPath)?.takeIf { it.isCalibrationFrame }

            val newBiasPath = (if (newDarkPath != null) null else if (useCalibrationGroup) calibrationFrameProvider
                .findBestBiasFrames(calibrationGroup, width, height, binX, binY)
                .firstOrNull()?.path else biasPath)?.takeIf { it.isCalibrationFrame }

            LOG.info(
                "live stacking will use calibration frames. group={}, dark={}, flat={}, bias={}",
                calibrationGroup, newDarkPath, newFlatPath, newBiasPath
            )

            copy(darkPath = newDarkPath, flatPath = newFlatPath, biasPath = newBiasPath)
        } else {
            this
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraLiveStackingManager>()

        private inline val Path?.isCalibrationFrame
            get() = this != null && exists() && isRegularFile() && (isFits() || isXisf())
    }
}
