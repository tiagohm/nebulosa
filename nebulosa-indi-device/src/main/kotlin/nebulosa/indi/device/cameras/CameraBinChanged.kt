package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraBinChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
