package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraExposureAborted(override val device: Camera) : CameraEvent, PropertyChangedEvent
