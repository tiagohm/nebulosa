package nebulosa.api.cameras

import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.camera.Camera
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
    @JvmField val capture: CameraStartCaptureRequest? = null,
) : MessageEvent {

    override val eventName = "CAMERA.CAPTURE_ELAPSED"
}
