package nebulosa.api.cameras

import nebulosa.batch.processing.Step
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

sealed interface CameraStartCaptureStep : Step {

    val camera: Camera

    val request: CameraStartCaptureRequest

    val savedPath: Path?

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean
}
