package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraHasCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
