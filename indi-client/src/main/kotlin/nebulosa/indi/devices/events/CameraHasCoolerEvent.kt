package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraHasCoolerEvent(override val device: Camera) : CameraEvent
