package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraAttachedEvent(override val device: Camera) : CameraEvent
