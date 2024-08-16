package nebulosa.api.cameras

import nebulosa.api.calibration.CalibrationFrameProvider
import nebulosa.api.livestacker.LiveStackingRequest
import nebulosa.api.stacker.StackerGroupType
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.livestacker.LiveStacker
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.util.EnumMap
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.extension

class CameraLiveStackingManager(
    private val calibrationFrameProvider: CalibrationFrameProvider? = null,
    private val minExposureAmount: Int = 2,
) {

    private val liveStackers = EnumMap<StackerGroupType, LiveStacker>(StackerGroupType::class.java)

    @Synchronized
    fun start(camera: Camera, request: CameraStartCaptureRequest): Boolean {
        if (request.stackerGroupType in liveStackers) {
            return true
        } else if (request.liveStacking.enabled && (request.isLoop || request.exposureAmount >= minExposureAmount)) {
            try {
                with(request.liveStacking.processCalibrationGroup(camera, request).get()) {
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
        if (path == null) return null

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
                    if (luminancePath != null) stacker.align(referencePath, luminancePath, luminancePath)
                    if (redPath != null) stacker.align(referencePath, redPath, redPath)
                    if (greenPath != null) stacker.align(referencePath, greenPath, greenPath)
                    if (bluePath != null) stacker.align(referencePath, bluePath, bluePath)
                }

                if (stacker.combineLRGB(combinedPath, luminancePath, redPath, greenPath, bluePath)) {
                    stackedPath = combinedPath
                }
            } else if (stackerGroupType == StackerGroupType.MONO && luminancePath != null) {
                if (stacker.combineLuminance(combinedPath, luminancePath, stackedPath, true)) {
                    stackedPath = combinedPath
                }
            } else if (stackerGroupType == StackerGroupType.RGB && luminancePath != null) {
                if (stacker.combineLuminance(combinedPath, luminancePath, stackedPath, false)) {
                    stackedPath = combinedPath
                }
            }
        }

        if (stackedPath == null) {
            stackedPath = liveStacker.stackedPath
        }

        if (stackedPath != null) {
            return stackedPath.copyTo(Path.of("${path.parent}", "STACKED.${stackerGroupType}.${stackedPath.extension}"), true)
        }

        return null
    }

    fun stop(request: CameraStartCaptureRequest) {
        liveStackers[request.stackerGroupType]?.stop()
    }

    private fun LiveStackingRequest.processCalibrationGroup(camera: Camera, request: CameraStartCaptureRequest): LiveStackingRequest {
        return if (calibrationFrameProvider != null && enabled &&
            !request.calibrationGroup.isNullOrBlank() && (darkPath == null || flatPath == null || biasPath == null)
        ) {
            val calibrationGroup = request.calibrationGroup
            val temperature = camera.temperature
            val binX = request.binX
            val binY = request.binY
            val width = request.width / binX
            val height = request.height / binY
            val exposureTime = request.exposureTime.toNanos() / 1000
            val gain = request.gain.toDouble()

            val wheel = camera.snoopedDevices.firstOrNull { it is FilterWheel } as? FilterWheel
            val filter = wheel?.let { it.names.getOrNull(it.position - 1) }

            LOG.info(
                "find calibration frames for live stacking. group={}, temperature={}, binX={}, binY={}. width={}, height={}, exposureTime={}, gain={}, filter={}",
                calibrationGroup, temperature, binX, binY, width, height, exposureTime, gain, filter
            )

            val newDarkPath = darkPath?.takeIf { it.exists() } ?: calibrationFrameProvider
                .findBestDarkFrames(calibrationGroup, temperature, width, height, binX, binY, exposureTime, gain)
                .firstOrNull()
                ?.path

            val newFlatPath = flatPath?.takeIf { it.exists() } ?: calibrationFrameProvider
                .findBestFlatFrames(calibrationGroup, width, height, binX, binY, filter)
                .firstOrNull()
                ?.path

            val newBiasPath = if (newDarkPath != null) null else biasPath?.takeIf { it.exists() } ?: calibrationFrameProvider
                .findBestBiasFrames(calibrationGroup, width, height, binX, binY)
                .firstOrNull()
                ?.path

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
    }
}
