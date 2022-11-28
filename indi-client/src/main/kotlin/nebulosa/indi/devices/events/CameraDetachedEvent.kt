package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraDetachedEvent(override val device: Camera) : CameraEvent
