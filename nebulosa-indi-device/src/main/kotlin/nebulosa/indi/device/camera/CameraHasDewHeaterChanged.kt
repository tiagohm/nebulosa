package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraHasDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
