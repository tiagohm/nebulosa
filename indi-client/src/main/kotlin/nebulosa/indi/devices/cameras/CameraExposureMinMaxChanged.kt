package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraExposureMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
