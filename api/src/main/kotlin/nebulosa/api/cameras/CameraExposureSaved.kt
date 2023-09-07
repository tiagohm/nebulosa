package nebulosa.api.cameras

import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class CameraExposureSaved(
    val image: Image?,
    override val camera: Camera,
    val path: Path?,
) : CameraCaptureEvent
