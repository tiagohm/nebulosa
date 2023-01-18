package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraHasDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
