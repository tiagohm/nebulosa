package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCoolerControlChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
