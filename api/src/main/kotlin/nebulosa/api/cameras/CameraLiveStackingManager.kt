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
) {

    private val liveStackers = EnumMap<StackerGroupType, LiveStacker>(StackerGroupType::class.java)

    @Synchronized
    private fun start(camera: Camera, request: CameraStartCaptureRequest): Boolean {
        if (request.stackerGroupType in liveStackers) {
            return true
        } else if (request.liveStacking.enabled && (request.isLoop || request.exposureAmount > 1)) {
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

    fun stack(camera: Camera, request: CameraStartCaptureRequest, path: Path?): Path? {
        if (path == null) return null

        val liveStacker = liveStackers[request.stackerGroupType] ?: return null
        val stacker = liveStacker.stacker

        if (start(camera, request)) {
            val stackedPath = liveStacker.add(path) ?: return null

            if (stacker != null) {
                val combinatedPath = Path.of("${path.parent}", "STACKED.fits")
                val luminancePath = liveStackers[StackerGroupType.LUMINANCE]?.stackedPath
                val redPath = liveStackers[StackerGroupType.RED]?.stackedPath
                val greenPath = liveStackers[StackerGroupType.GREEN]?.stackedPath
                val bluePath = liveStackers[StackerGroupType.BLUE]?.stackedPath

                if (luminancePath != null || redPath != null || greenPath != null || bluePath != null) {
                    if (stacker.combineLRGB(combinatedPath, luminancePath, redPath, greenPath, bluePath)) {
                        return combinatedPath
                    }
                }

                if (request.stackerGroupType == StackerGroupType.MONO && luminancePath != null) {
                    if (stacker.combineLuminance(combinatedPath, luminancePath, stackedPath, true)) {
                        return combinatedPath
                    }
                }

                if (request.stackerGroupType == StackerGroupType.RGB && luminancePath != null) {
                    if (stacker.combineLuminance(combinatedPath, luminancePath, stackedPath, false)) {
                        return combinatedPath
                    }
                }
            }
        }

        val stackedPath = liveStacker.stackedPath ?: return null
        return stackedPath.copyTo(Path.of("${path.parent}", "STACKED.${request.stackerGroupType}.${stackedPath.extension}"), true)
    }

    fun stop(request: CameraStartCaptureRequest) {
        liveStackers.remove(request.stackerGroupType)?.stop()
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
