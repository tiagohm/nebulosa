package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.SplitTask
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.livestacking.LiveStacker
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.Executor

data class CameraCaptureTask(
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
    @JvmField val guider: Guider? = null,
    private val useFirstExposure: Boolean = false,
    private val exposureMaxRepeat: Int = 0,
    private val executor: Executor? = null,
) : AbstractTask<CameraCaptureEvent>(), Consumer<Any>, CameraEventAware {

    private val delayTask = DelayTask(request.exposureDelay)
    private val waitForSettleTask = WaitForSettleTask(guider)
    private val delayAndWaitForSettleSplitTask = SplitTask(listOf(delayTask, waitForSettleTask), executor)
    private val cameraExposureTask = CameraExposureTask(camera, request)
    private val ditherAfterExposureTask = DitherAfterExposureTask(guider, request.dither)

    @Volatile private var exposureCount = 0
    @Volatile private var captureRemainingTime = Duration.ZERO
    @Volatile private var prevCaptureElapsedTime = Duration.ZERO
    @Volatile private var captureElapsedTime = Duration.ZERO
    @Volatile private var captureProgress = 0.0
    @Volatile private var stepRemainingTime = Duration.ZERO
    @Volatile private var stepElapsedTime = Duration.ZERO
    @Volatile private var stepProgress = 0.0
    @Volatile private var savedPath: Path? = null
    @Volatile private var liveStackedSavedPath: Path? = null

    @JvmField @JsonIgnore val estimatedCaptureTime: Duration = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(request.exposureTime.toNanos() * request.exposureAmount + request.exposureDelay.toNanos() * (request.exposureAmount - if (useFirstExposure) 0 else 1))

    @Volatile private var exposureRepeatCount = 0
    @Volatile private var liveStacker: LiveStacker? = null

    init {
        delayTask.subscribe(this)
        cameraExposureTask.subscribe(this)

        if (guider != null) {
            // waitForSettleTask.subscribe(this)
            ditherAfterExposureTask.subscribe(this)
        }
    }

    override fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("Camera Capture started. camera={}, request={}, exposureCount={}", camera, request, exposureCount)

        cameraExposureTask.reset()

        liveStacker?.close()
        liveStacker = null

        if (request.liveStacking.enabled && (request.isLoop || request.exposureAmount > 1 || exposureMaxRepeat > 1)) {
            try {
                liveStacker = request.liveStacking.get()
                liveStacker!!.start()
            } catch (e: Throwable) {
                LOG.error("failed to start live stacking. request={}", request.liveStacking, e)

                liveStacker?.close()
                liveStacker = null
            }
        }

        while (!cancellationToken.isCancelled &&
            !cameraExposureTask.isAborted &&
            ((exposureMaxRepeat > 0 && exposureRepeatCount < exposureMaxRepeat)
                    || (exposureMaxRepeat <= 0 && (request.isLoop || exposureCount < request.exposureAmount)))
        ) {
            if (exposureCount == 0) {
                sendEvent(CameraCaptureState.CAPTURE_STARTED)

                if (guider != null) {
                    if (useFirstExposure) {
                        // DELAY & WAIT FOR SETTLE.
                        delayAndWaitForSettleSplitTask.execute(cancellationToken)
                    } else {
                        // WAIT FOR SETTLE.
                        waitForSettleTask.execute(cancellationToken)
                    }
                } else if (useFirstExposure) {
                    // DELAY.
                    delayTask.execute(cancellationToken)
                }
            } else if (guider != null) {
                // DELAY & WAIT FOR SETTLE.
                delayAndWaitForSettleSplitTask.execute(cancellationToken)
            } else {
                // DELAY.
                delayTask.execute(cancellationToken)
            }

            // CAPTURE.
            cameraExposureTask.execute(cancellationToken)

            // DITHER.
            if (!cancellationToken.isCancelled && !cameraExposureTask.isAborted && guider != null
                && exposureCount >= 1 && exposureCount % request.dither.afterExposures == 0
            ) {
                ditherAfterExposureTask.execute(cancellationToken)
            }
        }

        sendEvent(CameraCaptureState.CAPTURE_FINISHED)

        liveStacker?.close()
        exposureRepeatCount = 0

        LOG.info("Camera Capture finished. camera={}, request={}, exposureCount={}", camera, request, exposureCount)
    }

    @Synchronized
    override fun accept(event: Any) {
        val state = when (event) {
            is DelayEvent -> {
                captureElapsedTime += event.waitTime
                stepElapsedTime = event.task.duration - event.remainingTime
                stepRemainingTime = event.remainingTime
                stepProgress = event.progress
                CameraCaptureState.WAITING
            }
            is CameraExposureEvent -> {
                when (event.state) {
                    CameraExposureState.STARTED -> {
                        prevCaptureElapsedTime = captureElapsedTime
                        exposureCount++
                        exposureRepeatCount++
                        CameraCaptureState.EXPOSURE_STARTED
                    }
                    CameraExposureState.ELAPSED -> {
                        captureElapsedTime = prevCaptureElapsedTime + event.elapsedTime
                        stepElapsedTime = event.elapsedTime
                        stepRemainingTime = event.remainingTime
                        stepProgress = event.progress
                        CameraCaptureState.EXPOSURING
                    }
                    CameraExposureState.FINISHED -> {
                        captureElapsedTime = prevCaptureElapsedTime + request.exposureTime
                        savedPath = event.savedPath
                        liveStackedSavedPath = addFrameToLiveStacker(savedPath)
                        CameraCaptureState.EXPOSURE_FINISHED
                    }
                    CameraExposureState.IDLE -> {
                        CameraCaptureState.CAPTURE_FINISHED
                    }
                }
            }
            else -> return LOG.warn("unknown event: {}", event)
        }

        sendEvent(state)
    }

    private fun sendEvent(state: CameraCaptureState) {
        if (state != CameraCaptureState.IDLE && !request.isLoop) {
            captureRemainingTime = if (estimatedCaptureTime > captureElapsedTime) estimatedCaptureTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedCaptureTime - captureRemainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
        }

        val event = CameraCaptureEvent(
            this, camera, state, request.exposureAmount, exposureCount,
            captureRemainingTime, captureElapsedTime, captureProgress,
            stepRemainingTime, stepElapsedTime, stepProgress,
            savedPath, liveStackedSavedPath
        )

        onNext(event)
    }

    private fun addFrameToLiveStacker(path: Path?): Path? {
        return liveStacker?.add(path ?: return null)
    }

    override fun close() {
        delayTask.close()
        waitForSettleTask.close()
        delayAndWaitForSettleSplitTask.close()
        cameraExposureTask.close()
        ditherAfterExposureTask.close()
        super.close()
    }

    override fun reset() {
        exposureCount = 0
        captureRemainingTime = Duration.ZERO
        prevCaptureElapsedTime = Duration.ZERO
        captureElapsedTime = Duration.ZERO
        captureProgress = 0.0
        stepRemainingTime = Duration.ZERO
        stepElapsedTime = Duration.ZERO
        stepProgress = 0.0
        savedPath = null
        liveStackedSavedPath = null

        delayTask.reset()
        cameraExposureTask.reset()
        ditherAfterExposureTask.reset()

        exposureRepeatCount = 0
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureTask>()
    }
}
