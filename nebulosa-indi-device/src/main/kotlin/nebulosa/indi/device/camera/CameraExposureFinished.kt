package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureFinished(override val device: Camera) : CameraEvent, PropertyChangedEvent
