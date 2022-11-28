package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CaptureFormat

data class CameraCaptureFormatEvent(
    override val device: Camera,
    val formats: List<CaptureFormat>,
) : CameraEvent
