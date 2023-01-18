package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
