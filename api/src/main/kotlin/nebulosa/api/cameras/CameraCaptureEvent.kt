package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.job.manager.TimedTaskEvent
import nebulosa.job.manager.delay.DelayEvent
import java.nio.file.Path

data class CameraCaptureEvent(
    @JvmField val camera: Camera,
    @JvmField var state: CameraCaptureState = CameraCaptureState.IDLE,
    @JvmField var exposureAmount: Int = 0,
    @JvmField var exposureCount: Int = 0,
    @JvmField var captureRemainingTime: Long = 0L,
    @JvmField var captureElapsedTime: Long = 0L,
    @JvmField var captureProgress: Double = 0.0,
    @JvmField var stepRemainingTime: Long = 0L,
    @JvmField var stepElapsedTime: Long = 0L,
    @JvmField var stepProgress: Double = 0.0,
    @JvmField var savedPath: Path? = null,
    @JvmField var liveStackedPath: Path? = null,
) : MessageEvent {

    override val eventName = "CAMERA.CAPTURE_ELAPSED"

    @Volatile @field:JsonIgnore private var captureStartElapsedTime = 0L
    @Volatile @field:JsonIgnore private var captureStartRemainingTime = 0L

    @Suppress("NOTHING_TO_INLINE")
    private inline fun handleTimedTaskEvent(event: TimedTaskEvent) {
        stepRemainingTime = event.remainingTime
        stepElapsedTime = event.elapsedTime
        stepProgress = event.progress
    }

    fun handleCameraExposureStarted(event: CameraExposureStarted) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURE_STARTED
        captureStartElapsedTime = captureElapsedTime
        captureStartRemainingTime = captureRemainingTime
        exposureCount++
    }

    fun handleCameraExposureFinished(event: CameraExposureFinished) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURE_FINISHED
        captureElapsedTime = captureStartElapsedTime + event.elapsedTime
        captureRemainingTime = captureStartRemainingTime - event.elapsedTime
        savedPath = event.savedPath
    }

    fun handleCameraExposureElapsed(event: CameraExposureElapsed) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURING
        captureElapsedTime = captureStartElapsedTime + event.elapsedTime
        captureRemainingTime = captureStartRemainingTime - event.elapsedTime
    }

    fun handleCameraCaptureStarted(estimatedCaptureTime: Long = 0L) {
        state = CameraCaptureState.CAPTURE_STARTED
        captureRemainingTime = estimatedCaptureTime
        captureElapsedTime = 0L
        captureProgress = 0.0
    }

    fun handleCameraCaptureFinished() {
        state = CameraCaptureState.CAPTURE_FINISHED
        captureRemainingTime = 0L
        captureProgress = 1.0
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun computeCaptureProgress() {
        if (captureRemainingTime > 0L) {
            captureProgress = captureElapsedTime.toDouble() / (captureElapsedTime + captureRemainingTime)
        }
    }

    fun handleCameraDelayEvent(event: DelayEvent, newState: CameraCaptureState = CameraCaptureState.WAITING) {
        handleTimedTaskEvent(event)
        captureElapsedTime += event.waitTime
        captureRemainingTime -= event.waitTime
        state = newState
        computeCaptureProgress()
    }

    fun handleCameraExposureEvent(event: CameraExposureEvent) {
        when (event) {
            is CameraExposureElapsed -> handleCameraExposureElapsed(event)
            is CameraExposureFinished -> handleCameraExposureFinished(event)
            is CameraExposureStarted -> handleCameraExposureStarted(event)
        }

        computeCaptureProgress()
    }
}
