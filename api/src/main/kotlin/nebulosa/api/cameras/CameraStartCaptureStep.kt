package nebulosa.api.cameras

import nebulosa.batch.processing.Step

sealed interface CameraStartCaptureStep : Step {

    val request: CameraStartCaptureRequest

    fun registerCameraCaptureListener(listener: CameraCaptureListener): Boolean

    fun unregisterCameraCaptureListener(listener: CameraCaptureListener): Boolean
}
