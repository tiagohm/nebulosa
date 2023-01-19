package nebulosa.desktop.logic.camera

import nebulosa.indi.device.cameras.Camera
import nebulosa.indi.device.cameras.CameraEvent
import java.nio.file.Path

data class CameraFrameSaved(
    override val device: Camera,
    val imagePath: Path,
    val temporary: Boolean = false,
) : CameraEvent
