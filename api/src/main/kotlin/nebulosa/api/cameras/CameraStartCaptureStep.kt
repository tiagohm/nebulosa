package nebulosa.api.cameras

import nebulosa.batch.processing.Step
import java.nio.file.Path

sealed interface CameraStartCaptureStep : Step {

    val request: CameraStartCaptureRequest

    val savedPath: Path?

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean
}
