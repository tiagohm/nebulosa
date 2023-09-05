package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraDelayUpdated(
    val camera: Camera,
    val jobId: Long,
    val waitProgress: Double,
    val waitRemainingTime: Long,
)
