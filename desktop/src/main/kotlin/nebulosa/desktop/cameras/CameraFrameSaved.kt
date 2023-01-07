package nebulosa.desktop.cameras

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraEvent
import java.nio.file.Path

data class CameraFrameSaved(
    override val device: Camera,
    val imagePath: Path,
    val temporary: Boolean = false,
) : CameraEvent
