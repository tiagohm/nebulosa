package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraDelayUpdated(
    override val camera: Camera,
    val waitProgress: Double,
    val waitRemainingTime: Long,
    val waitTime: Long,
) : CameraCaptureEvent
