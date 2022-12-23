package nebulosa.indi.devices.cameras

import java.io.InputStream

data class CameraExposureFrame(
    override val device: Camera,
    val fits: InputStream,
) : CameraEvent
