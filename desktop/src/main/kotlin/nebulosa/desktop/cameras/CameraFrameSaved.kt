package nebulosa.desktop.cameras

import nebulosa.indi.device.cameras.Camera
import nebulosa.indi.device.cameras.CameraEvent
import java.nio.file.Path

data class CameraFrameSaved(
    override val device: Camera,
    val imagePath: Path,
    val temporary: Boolean = false,
) : CameraEvent
