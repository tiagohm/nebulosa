package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraHasDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
