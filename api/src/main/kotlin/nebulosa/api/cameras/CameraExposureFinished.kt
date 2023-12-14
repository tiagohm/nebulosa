package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class CameraExposureFinished(
    override val camera: Camera,
    override val progress: Double,
    val savePath: Path,
) : CameraExposureEvent {

    override val eventName = "CAMERA_EXPOSURE_FINISHED"
}
