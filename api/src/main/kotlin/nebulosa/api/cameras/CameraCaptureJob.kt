package nebulosa.api.cameras

import nebulosa.api.guiding.DitherAfterExposureEvent
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.wheels.WheelEventAware
import nebulosa.api.wheels.WheelMoveTask
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.job.manager.AbstractJob
import nebulosa.job.manager.SplitTask
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayEvent
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.nio.file.Path

data class CameraCaptureJob(
    @JvmField val cameraCaptureExecutor: CameraCaptureExecutor,
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
    @JvmField val guider: Guider? = null,
    @JvmField val liveStackerManager: CameraLiveStackingManager? = null,
    @JvmField val mount: Mount? = null,
    @JvmField val wheel: FilterWheel? = null,
    @JvmField val focuser: Focuser? = null,
    @JvmField val rotator: Rotator? = null,
) : AbstractJob(), CameraEventAware, WheelEventAware {

    private val delayTask = DelayTask(this, request.exposureDelay)
    private val waitForSettleTask = WaitForSettleTask(this, guider)
    private val delayAndWaitForSettleSplitTask = SplitTask(listOf(delayTask, waitForSettleTask), cameraCaptureExecutor)
    private val cameraExposureTask = CameraExposureTask(this, camera, request)
    private val ditherAfterExposureTask = DitherAfterExposureTask(this, guider, request.dither)
    private val shutterWheelMoveTask = if (wheel != null && request.shutterPosition > 0) WheelMoveTask(this, wheel, request.shutterPosition) else null

    @JvmField val status = CameraCaptureEvent(camera)
    @JvmField val estimatedCaptureTime =
        (request.exposureTime.toNanos() * request.exposureAmount + request.exposureDelay.toNanos() * (request.exposureAmount - 1)) / 1000L

    @Volatile private var startCaptureElapsedTime = 0L

    init {
        status.exposureAmount = request.exposureAmount

        add(delayTask)
        add(delayAndWaitForSettleSplitTask)
        add(cameraExposureTask)
        add(ditherAfterExposureTask)
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        shutterWheelMoveTask?.handleFilterWheelEvent(event)
    }

    override fun beforeStart() {
        LOG.debug { "Camera Capture started. request=$request, camera=$camera, mount=$mount, wheel=$wheel, focuser=$focuser" }

        camera.snoop(listOf(mount, wheel, focuser, rotator))

        status.state = CameraCaptureState.CAPTURE_STARTED
        status.captureRemainingTime = estimatedCaptureTime
        status.captureElapsedTime = 0L
        status.captureProgress = 0.0
        cameraCaptureExecutor.accept(status)

        if (request.frameType == FrameType.DARK) {
            shutterWheelMoveTask?.run()
        }
    }

    override fun afterFinish() {
        status.state = CameraCaptureState.CAPTURE_FINISHED
        status.captureRemainingTime = 0L
        status.captureElapsedTime = estimatedCaptureTime
        status.captureProgress = 1.0
        cameraCaptureExecutor.accept(status)

        liveStackerManager?.stop(request)

        LOG.debug { "Camera Capture finished. request=$request, status=$status, mount=$mount, wheel=$wheel, focuser=$focuser" }
    }

    override fun isLoop(): Boolean {
        return request.isLoop
    }

    override fun canRun(prev: Task?, current: Task): Boolean {
        if (current === ditherAfterExposureTask) {
            return !isCancelled && guider != null
                    && status.exposureCount >= 1 && request.dither.afterExposures > 0
                    && status.exposureCount % request.dither.afterExposures == 0
        } else if (current === delayTask) {
            return status.exposureCount == 0
        } else if (current === delayAndWaitForSettleSplitTask) {
            return status.exposureCount > 0
        }

        return super.canRun(prev, current)
    }

    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                if (event.task === delayTask) {
                    status.handleCameraDelayEvent(event)
                    status.captureElapsedTime += event.waitTime
                    status.captureRemainingTime -= event.waitTime
                }
            }
            is CameraExposureEvent -> {
                status.handleCameraExposureEvent(event)

                if (event is CameraExposureStarted) {
                    startCaptureElapsedTime = status.captureElapsedTime
                } else {
                    status.captureElapsedTime = startCaptureElapsedTime + event.elapsedTime

                    if (event is CameraExposureFinished) {
                        status.liveStackedPath = addFrameToLiveStacker(status.savedPath)
                    }
                }

                if (estimatedCaptureTime > 0L) {
                    status.captureProgress = (estimatedCaptureTime - status.captureRemainingTime) / estimatedCaptureTime.toDouble()
                }

                cameraCaptureExecutor.accept(status)
            }
            is DitherAfterExposureEvent -> {
                status.state = CameraCaptureState.DITHERING
                cameraCaptureExecutor.accept(status)
            }
        }
    }

    private fun addFrameToLiveStacker(path: Path?): Path? {
        return if (path != null && liveStackerManager?.start(request, path) == true) {
            try {
                status.state = CameraCaptureState.STACKING
                cameraCaptureExecutor.accept(status)

                liveStackerManager.stack(request, path)
            } catch (_: Throwable) {
                null
            }
        } else {
            null
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureJob>()
    }
}
