package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureStarted(
    override val camera: Camera,
) : CameraCaptureEvent {

    override val progress = 0.0

    override val eventName = "CAMERA_CAPTURE_STARTED"
}
