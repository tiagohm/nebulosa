package nebulosa.api.cameras

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

    private fun handleTimedTaskEvent(event: TimedTaskEvent) {
        stepRemainingTime = event.remainingTime
        stepElapsedTime = event.elapsedTime
        stepProgress = event.progress
    }

    fun handleCameraExposureStarted(event: CameraExposureStarted) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURE_STARTED
        exposureCount++
    }

    fun handleCameraExposureFinished(event: CameraExposureFinished) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURE_FINISHED
        savedPath = event.savedPath
    }

    fun handleCameraExposureElapsed(event: CameraExposureElapsed) {
        handleTimedTaskEvent(event)
        state = CameraCaptureState.EXPOSURING
    }

    fun handleCameraExposureEvent(event: CameraExposureEvent) {
        when (event) {
            is CameraExposureElapsed -> handleCameraExposureElapsed(event)
            is CameraExposureFinished -> handleCameraExposureFinished(event)
            is CameraExposureStarted -> handleCameraExposureStarted(event)
        }
    }

    fun handleCameraDelayEvent(event: DelayEvent, newState: CameraCaptureState = CameraCaptureState.WAITING) {
        handleTimedTaskEvent(event)
        state = newState
    }
}
