package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraCanSubframeEvent(
    override val device: Camera,
    val enabled: Boolean,
) : CameraEvent
