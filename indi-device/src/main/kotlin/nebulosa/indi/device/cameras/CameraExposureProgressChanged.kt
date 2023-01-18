package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureProgressChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
