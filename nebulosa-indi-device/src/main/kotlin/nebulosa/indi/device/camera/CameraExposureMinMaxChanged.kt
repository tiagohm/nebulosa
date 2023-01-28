package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
