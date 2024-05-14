package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.time.Duration

data class CameraCaptureTask(
    @JvmField val camera: Camera,
    @JvmField val request: CameraStartCaptureRequest,
    @JvmField val guider: Guider? = null,
    private val useFirstExposure: Boolean = false,
    private val exposureMaxRepeat: Int = 0,
) : AbstractTask<CameraCaptureEvent>(), Consumer<Any> {

    private val delayTask = DelayTask(request.exposureDelay)
    private val waitForSettleTask = WaitForSettleTask(guider)
    private val delayAndWaitForSettleSplitTask = DelayAndWaitForSettleTask(delayTask, waitForSettleTask)
    private val cameraExposureTask = CameraExposureTask(camera, request)
    private val ditherAfterExposureTask = DitherAfterExposureTask(guider, request.dither)

    @Volatile private var state = CameraCaptureState.IDLE
    @Volatile private var exposureCount = 0
    @Volatile private var captureRemainingTime = Duration.ZERO
    @Volatile private var prevCaptureElapsedTime = Duration.ZERO
    @Volatile private var captureElapsedTime = Duration.ZERO
    @Volatile private var captureProgress = 0.0
    @Volatile private var stepRemainingTime = Duration.ZERO
    @Volatile private var stepElapsedTime = Duration.ZERO
    @Volatile private var stepProgress = 0.0
    @Volatile private var savePath: Path? = null

    @JvmField @JsonIgnore val estimatedCaptureTime: Duration = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(request.exposureTime.toNanos() * request.exposureAmount + request.exposureDelay.toNanos() * (request.exposureAmount - if (useFirstExposure) 0 else 1))

    @Volatile private var exposureRepeatCount = 0

    init {
        delayTask.subscribe(this)
        cameraExposureTask.subscribe(this)

        if (guider != null) {
            // waitForSettleTask.subscribe(this)
            ditherAfterExposureTask.subscribe(this)
        }
    }

    fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("Camera Capture started. camera={}, request={}, exposureCount={}", camera, request, exposureCount)

        cameraExposureTask.reset()

        while (!cancellationToken.isDone &&
            !cameraExposureTask.isAborted &&
            ((exposureMaxRepeat > 0 && exposureRepeatCount < exposureMaxRepeat)
                    || (exposureMaxRepeat <= 0 && (request.isLoop || exposureCount < request.exposureAmount)))
        ) {
            if (exposureCount == 0) {
                state = CameraCaptureState.CAPTURE_STARTED
                sendEvent()

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
            if (!cancellationToken.isDone && !cameraExposureTask.isAborted && guider != null
                && exposureCount >= 1 && exposureCount % request.dither.afterExposures == 0
            ) {
                ditherAfterExposureTask.execute(cancellationToken)
            }
        }

        if (state != CameraCaptureState.CAPTURE_FINISHED) {
            state = CameraCaptureState.CAPTURE_FINISHED
            sendEvent()
        }

        exposureRepeatCount = 0

        LOG.info("Camera Capture finished. camera={}, request={}, exposureCount={}", camera, request, exposureCount)
    }

    @Synchronized
    override fun accept(event: Any) {
        when (event) {
            is DelayEvent -> {
                state = CameraCaptureState.WAITING
                captureElapsedTime += event.waitTime
                stepElapsedTime = event.task.duration - event.remainingTime
                stepRemainingTime = event.remainingTime
                stepProgress = event.progress
            }
            is CameraExposureEvent -> {
                when (event.state) {
                    CameraExposureState.STARTED -> {
                        state = CameraCaptureState.EXPOSURE_STARTED
                        prevCaptureElapsedTime = captureElapsedTime
                        exposureCount++
                        exposureRepeatCount++
                    }
                    CameraExposureState.ELAPSED -> {
                        state = CameraCaptureState.EXPOSURING
                        captureElapsedTime = prevCaptureElapsedTime + event.elapsedTime
                        stepElapsedTime = event.elapsedTime
                        stepRemainingTime = event.remainingTime
                        stepProgress = event.progress
                    }
                    CameraExposureState.FINISHED -> {
                        state = CameraCaptureState.EXPOSURE_FINISHED
                        captureElapsedTime = prevCaptureElapsedTime + request.exposureTime
                        savePath = event.savedPath
                    }
                    CameraExposureState.IDLE -> {
                        state = CameraCaptureState.CAPTURE_FINISHED
                    }
                }
            }
            else -> return LOG.warn("unknown event: {}", event)
        }

        sendEvent()
    }

    private fun sendEvent() {
        if (state != CameraCaptureState.IDLE && !request.isLoop) {
            captureRemainingTime = if (estimatedCaptureTime > captureElapsedTime) estimatedCaptureTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedCaptureTime - captureRemainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
        }

        val event = CameraCaptureEvent(
            camera, state, request.exposureAmount, exposureCount,
            captureRemainingTime, captureElapsedTime, captureProgress,
            stepRemainingTime, stepElapsedTime, stepProgress,
            savePath
        )

        onNext(event)
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
        state = CameraCaptureState.IDLE
        exposureCount = 0
        captureRemainingTime = Duration.ZERO
        prevCaptureElapsedTime = Duration.ZERO
        captureElapsedTime = Duration.ZERO
        captureProgress = 0.0
        stepRemainingTime = Duration.ZERO
        stepElapsedTime = Duration.ZERO
        stepProgress = 0.0
        savePath = null

        delayTask.reset()
        cameraExposureTask.reset()
        ditherAfterExposureTask.reset()

        exposureRepeatCount = 0
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureTask>()
    }
}
