package nebulosa.api.cameras

import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path
import java.time.Duration

sealed interface CameraCaptureEvent : MessageEvent {

    val camera: Camera

    val state: CameraCaptureState

    val exposureAmount: Int

    val exposureCount: Int

    val captureElapsedTime: Duration

    val captureProgress: Double

    val captureRemainingTime: Duration

    val exposureProgress: Double

    val exposureRemainingTime: Duration

    val waitRemainingTime: Duration

    val waitProgress: Double

    val savePath: Path?

    override val eventName
        get() = "CAMERA_CAPTURE_ELAPSED"
}
