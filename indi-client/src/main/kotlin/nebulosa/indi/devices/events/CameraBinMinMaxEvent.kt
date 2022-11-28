package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraBinMinMaxEvent(
    override val device: Camera,
    val maxX: Int, val maxY: Int,
) : CameraEvent
