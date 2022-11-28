package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CfaPattern

data class CameraCfaEvent(
    override val device: Camera,
    val offsetX: Int,
    val offsetY: Int,
    val type: CfaPattern,
) : CameraEvent
