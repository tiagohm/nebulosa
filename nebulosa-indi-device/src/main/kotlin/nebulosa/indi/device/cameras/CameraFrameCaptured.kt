package nebulosa.indi.device.cameras

import java.io.InputStream

data class CameraFrameCaptured(
    override val device: Camera,
    val fits: InputStream,
) : CameraEvent
