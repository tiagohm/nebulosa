package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraExposureMinMaxEvent(
    override val device: Camera,
    val min: Long, val max: Long,
) : CameraEvent
