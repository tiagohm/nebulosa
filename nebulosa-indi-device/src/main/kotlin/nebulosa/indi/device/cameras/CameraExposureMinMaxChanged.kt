package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
