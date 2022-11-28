package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraActivatedEvent(override val device: Camera) : CameraEvent
