package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureFailed(override val device: Camera) : CameraEvent, PropertyChangedEvent
