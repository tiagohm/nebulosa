package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCfaChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
