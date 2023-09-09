package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class CameraExposureFinished(
    override val camera: Camera,
    @JsonIgnore val image: Image?,
    val path: Path?,
) : CameraCaptureEvent
