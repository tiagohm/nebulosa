package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraBinChangedEvent(
    override val device: Camera,
    val x: Int, val y: Int,
) : CameraEvent
