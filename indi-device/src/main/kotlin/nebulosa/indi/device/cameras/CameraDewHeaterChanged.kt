package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
