package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.guiding.DitherAfterExposureEvent
import nebulosa.api.guiding.DitherAfterExposureTask
import nebulosa.api.guiding.WaitForSettleEvent
import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.tasks.Task
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
) : Task<CameraCaptureEvent>(), Consumer<Any> {

    private val delayTask = DelayTask(request.exposureDelay)
    private val waitForSettleTask = WaitForSettleTask(guider)
    private val delayAndWaitForSettleSplitTask = DelayAndWaitForSettleTask(delayTask, waitForSettleTask)
    private val cameraExposureTask = CameraExposureTask(camera, request)
    private val ditherAfterExposureTask = DitherAfterExposureTask(guider, request.dither)

    @Volatile private var state = CameraCaptureState.IDLE
    @Volatile private var exposureCount = 0
    @Volatile private var captureRemainingTime: Duration = Duration.ZERO
    @Volatile private var prevCaptureElapsedTime: Duration = Duration.ZERO
    @Volatile private var captureElapsedTime: Duration = Duration.ZERO
    @Volatile private var captureProgress: Double = 0.0
    @Volatile private var stepRemainingTime: Duration = Duration.ZERO
    @Volatile private var stepElapsedTime: Duration = Duration.ZERO
    @Volatile private var stepProgress: Double = 0.0
    @Volatile private var savePath: Path? = null

    @JvmField @JsonIgnore val estimatedCaptureTime: Duration = if (request.isLoop) Duration.ZERO
    else Duration.ofNanos(request.exposureTime.toNanos() * request.exposureAmount + request.exposureDelay.toNanos() * (request.exposureAmount - 1))

    init {
        delayTask.subscribe(this)
        cameraExposureTask.subscribe(this)

        if (guider != null) {
            waitForSettleTask.subscribe(this)
            ditherAfterExposureTask.subscribe(this)
        }
    }

    fun handleCameraEvent(event: CameraEvent) {
        cameraExposureTask.handleCameraEvent(event)
    }

    override fun execute(cancellationToken: CancellationToken) {
        LOG.info("camera capture started. camera={}, request={}", camera, request)

        while (!cancellationToken.isDone &&
            (request.isLoop || exposureCount < request.exposureAmount)
        ) {
            if (exposureCount == 0) {
                if (guider != null) {
                    // WAIT FOR SETTLE.
                    waitForSettleTask.execute(cancellationToken)
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
            if (guider != null && exposureCount >= 1 && exposureCount % request.dither.afterExposures == 0) {
                ditherAfterExposureTask.execute(cancellationToken)
            }
        }
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
            is WaitForSettleEvent -> {
                when (event) {
                    is WaitForSettleEvent.Started -> return
                    is WaitForSettleEvent.Finished -> return
                }
            }
            is CameraExposureEvent -> {
                when (event.state) {
                    CameraExposureState.STARTED -> {
                        state = CameraCaptureState.EXPOSURE_STARTED
                        prevCaptureElapsedTime = captureElapsedTime
                        exposureCount++
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
                        savePath = event.savePath
                    }
                    CameraExposureState.IDLE,
                    CameraExposureState.ABORTED -> {
                        state = CameraCaptureState.IDLE
                    }
                }
            }
            is DitherAfterExposureEvent -> {
                when (event) {
                    is DitherAfterExposureEvent.Started -> return
                    is DitherAfterExposureEvent.Dithered -> return
                    is DitherAfterExposureEvent.Finished -> return
                }
            }
            else -> return LOG.warn("unknown event: {}", event)
        }

        if (state != CameraCaptureState.IDLE && !request.isLoop) {
            captureRemainingTime = if (estimatedCaptureTime > captureElapsedTime) estimatedCaptureTime - captureElapsedTime else Duration.ZERO
            captureProgress = (estimatedCaptureTime - captureRemainingTime).toNanos().toDouble() / estimatedCaptureTime.toNanos()
        }

        val cameraCaptureEvent = CameraCaptureEvent(
            camera, state, exposureCount,
            captureRemainingTime, captureElapsedTime, captureProgress,
            stepRemainingTime, stepElapsedTime, stepProgress,
            savePath
        )

        onNext(cameraCaptureEvent)
    }

    override fun close() {
        delayTask.close()
        waitForSettleTask.close()
        delayAndWaitForSettleSplitTask.close()
        cameraExposureTask.close()
        ditherAfterExposureTask.close()
        super.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraCaptureTask>()
    }
}
