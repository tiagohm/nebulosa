package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCfaChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
