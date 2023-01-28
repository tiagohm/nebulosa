package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
