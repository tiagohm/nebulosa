package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureAborted(override val device: Camera) : CameraEvent, PropertyChangedEvent
