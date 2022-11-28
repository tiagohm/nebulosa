package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera
import java.io.InputStream

data class CameraExposureFrameEvent(
    override val device: Camera,
    val fits: InputStream,
) : CameraEvent
