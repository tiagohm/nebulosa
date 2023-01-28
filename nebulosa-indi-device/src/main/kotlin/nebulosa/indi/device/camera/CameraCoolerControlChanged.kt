package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCoolerControlChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
