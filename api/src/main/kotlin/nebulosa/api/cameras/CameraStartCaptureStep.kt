package nebulosa.api.cameras

import nebulosa.batch.processing.Step
import nebulosa.indi.device.camera.Camera

sealed interface CameraStartCaptureStep : Step {

    val camera: Camera

    val request: CameraStartCaptureRequest

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean
}
