package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraBinChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
