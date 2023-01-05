package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraExposureFinished(override val device: Camera) : CameraEvent, PropertyChangedEvent
