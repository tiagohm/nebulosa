package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureFinished(override val device: Camera) : CameraEvent, PropertyChangedEvent
