package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

data class CameraExposureDelayEvent(
    val camera: Camera,
    val waitProgress: Double,
    val waitRemainingTime: Long,
    val waitTime: Long,
)
