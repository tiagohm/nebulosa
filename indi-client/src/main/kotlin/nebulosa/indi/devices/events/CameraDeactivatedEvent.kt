package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraDeactivatedEvent(override val device: Camera) : CameraEvent
