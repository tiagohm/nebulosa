package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureIsWaiting(
    override val camera: Camera,
    override val progress: Double,
) : CameraCaptureEvent {

    override val eventName = "CAMERA_CAPTURE_WAITING"
}
