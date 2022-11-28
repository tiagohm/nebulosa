package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraExposureAbortedEvent(override val device: Camera) : CameraEvent
