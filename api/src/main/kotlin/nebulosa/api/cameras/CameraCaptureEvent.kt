package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path
import java.time.Duration

data class CameraCaptureEvent(
    @JvmField @field:JsonIgnore val task: CameraCaptureTask,
    @JvmField val camera: Camera = task.camera,
    @JvmField val state: CameraCaptureState = CameraCaptureState.IDLE,
    @JvmField val exposureAmount: Int = 0,
    @JvmField val exposureCount: Int = 0,
    @JvmField val captureRemainingTime: Duration = Duration.ZERO,
    @JvmField val captureElapsedTime: Duration = Duration.ZERO,
    @JvmField val captureProgress: Double = 0.0,
    @JvmField val stepRemainingTime: Duration = Duration.ZERO,
    @JvmField val stepElapsedTime: Duration = Duration.ZERO,
    @JvmField val stepProgress: Double = 0.0,
    @JvmField val savedPath: Path? = null,
    @JvmField val liveStackedSavedPath: Path? = null,
) : MessageEvent {

    override val eventName = "CAMERA.CAPTURE_ELAPSED"
}
