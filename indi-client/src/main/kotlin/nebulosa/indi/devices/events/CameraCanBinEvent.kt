package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraCanBinEvent(
    override val device: Camera,
    val enabled: Boolean,
) : CameraEvent
