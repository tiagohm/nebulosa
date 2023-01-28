package nebulosa.indi.device.camera

import java.io.InputStream

data class CameraFrameCaptured(
    override val device: Camera,
    val fits: InputStream,
) : CameraEvent
