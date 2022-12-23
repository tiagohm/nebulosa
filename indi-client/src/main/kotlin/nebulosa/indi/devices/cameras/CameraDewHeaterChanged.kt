package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraDewHeaterChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
