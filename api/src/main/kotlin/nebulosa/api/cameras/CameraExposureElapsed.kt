package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureElapsed(
    override val camera: Camera,
    override val progress: Double,
) : CameraExposureEvent {

    override val eventName = "CAMERA_EXPOSURE_ELAPSED"
}
