package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
