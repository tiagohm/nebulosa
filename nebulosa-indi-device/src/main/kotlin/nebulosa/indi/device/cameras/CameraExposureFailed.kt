package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureFailed(override val device: Camera) : CameraEvent, PropertyChangedEvent
