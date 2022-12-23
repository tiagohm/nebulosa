package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraHasCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
