package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposureStateChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
