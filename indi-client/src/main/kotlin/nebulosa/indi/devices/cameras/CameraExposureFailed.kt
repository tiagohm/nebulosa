package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraExposureFailed(override val device: Camera) : CameraEvent, PropertyChangedEvent
