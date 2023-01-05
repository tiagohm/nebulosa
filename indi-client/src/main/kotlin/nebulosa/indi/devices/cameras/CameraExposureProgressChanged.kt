package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraExposureProgressChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
