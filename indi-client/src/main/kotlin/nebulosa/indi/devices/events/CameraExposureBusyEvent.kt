package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraExposureBusyEvent(override val device: Camera, val exposure: Long) : CameraEvent
