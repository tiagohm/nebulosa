package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraCaptureFinished(
    override val camera: Camera,
) : CameraCaptureEvent {

    override val progress = 1.0

    override val eventName = "CAMERA_CAPTURE_FINISHED"
}
