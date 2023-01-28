package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureProgressChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
