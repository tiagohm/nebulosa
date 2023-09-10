package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.nio.file.Path

data class CameraExposureFinishEvent(val camera: Camera, val path: Path?)
