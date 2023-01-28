package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraGainMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
