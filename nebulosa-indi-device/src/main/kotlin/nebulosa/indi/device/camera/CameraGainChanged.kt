package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraGainChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
