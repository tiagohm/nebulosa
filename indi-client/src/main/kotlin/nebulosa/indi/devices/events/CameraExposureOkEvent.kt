package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraExposureOkEvent(override val device: Camera) : CameraEvent
