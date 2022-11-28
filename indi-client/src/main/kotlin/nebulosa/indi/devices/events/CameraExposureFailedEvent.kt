package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraExposureFailedEvent(override val device: Camera) : CameraEvent
