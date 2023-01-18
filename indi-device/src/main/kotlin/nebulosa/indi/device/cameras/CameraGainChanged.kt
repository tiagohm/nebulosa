package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraGainChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
