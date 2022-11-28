package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraCoolerToggledEvent(
    override val device: Camera,
    val enabled: Boolean,
) : CameraEvent
