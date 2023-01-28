package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraHasCoolerChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
